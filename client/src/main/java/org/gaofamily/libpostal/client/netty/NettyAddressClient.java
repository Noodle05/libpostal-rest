package org.gaofamily.libpostal.client.netty;

import com.eaio.uuid.UUID;
import org.gaofamily.libpostal.client.AddressClient;
import org.gaofamily.libpostal.client.AddressException;
import org.gaofamily.libpostal.client.LimitExceededException;
import org.gaofamily.libpostal.client.NoAvailabeServerException;
import org.gaofamily.libpostal.model.AddressDataModelProtos;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public class NettyAddressClient implements AddressClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyAddressClient.class);
    private static final String THREADPOOL_PREFIX = "NettyClient-";
    private static final int DEFAULT_THREAD_NUMBER = Runtime.getRuntime().availableProcessors() * 2;
    private static final int DEFAULT_CONNECTION_NUMBER = 1;
    private static final int MAX_ADDRESSES_PER_REQUEST = 100;
    private static final long CLIENT_TIMEOUT = 5000L;

    private final ExecutorService threadPool;
    private final ScheduledExecutorService delayer;
    private final boolean threadPoolOwner;
    private final ChannelPoolMap<InetSocketAddress, FixedChannelPool> poolMap;
    private EventLoopGroup workerGroup;
    private final List<InetSocketAddress> socketAddresses;
    private final Lock socketReadLock;
    private final Lock socketWriteLock;
    private final AtomicInteger socketIndex;

    public NettyAddressClient(String host, int port) throws InterruptedException, UnknownHostException {
        this(DEFAULT_CONNECTION_NUMBER, DEFAULT_THREAD_NUMBER, null, new InetSocketAddress(host, port));
    }

    public NettyAddressClient(String host, int port, int numberOfConnection) throws InterruptedException, UnknownHostException {
        this(numberOfConnection, DEFAULT_THREAD_NUMBER, null, new InetSocketAddress(host, port));
    }

    public NettyAddressClient(String host, int port, int numberOfThread, ExecutorService threadPool) throws InterruptedException, UnknownHostException {
        this(DEFAULT_CONNECTION_NUMBER, numberOfThread, threadPool, new InetSocketAddress(host, port));
    }

    public NettyAddressClient(String host, int port, int numberOfConnection, int numberOfThread, ExecutorService threadPool) throws InterruptedException, UnknownHostException {
        this(numberOfConnection, numberOfThread, threadPool, new InetSocketAddress(host, port));
    }

    public NettyAddressClient(InetSocketAddress... addresses) throws InterruptedException, UnknownHostException {
        this(DEFAULT_CONNECTION_NUMBER, DEFAULT_THREAD_NUMBER, null, addresses);
    }

    public NettyAddressClient(InetSocketAddress[] socketAddress, int numberOfConnection) throws UnknownHostException, InterruptedException {
        this(numberOfConnection, DEFAULT_THREAD_NUMBER, null, socketAddress);
    }

    public NettyAddressClient(InetSocketAddress[] socketAddress, int numberOfConnection, int numberOfThread) throws UnknownHostException, InterruptedException {
        this(numberOfConnection, numberOfThread, null, socketAddress);
    }

    public NettyAddressClient(InetSocketAddress[] socketAddress, int numberOfConnection, int numberOfThread, ExecutorService threadPool) throws UnknownHostException, InterruptedException {
        this(numberOfConnection, numberOfThread, threadPool, socketAddress);
    }

    private NettyAddressClient(int numberOfConnection, int numberOfThread, ExecutorService threadPool, InetSocketAddress... serverAddresses) throws InterruptedException, UnknownHostException {
        assert serverAddresses != null;
        assert serverAddresses.length > 0;
        this.socketAddresses = Arrays.asList(serverAddresses);
        ReadWriteLock rwl = new ReentrantReadWriteLock();
        socketReadLock = rwl.readLock();
        socketWriteLock = rwl.writeLock();
        socketIndex = new AtomicInteger(0);
        ExecutorService tp = threadPool;
        boolean tpOwner = false;
        if (tp == null) {
            AtomicInteger counter = new AtomicInteger(0);
            ThreadPoolExecutor tp1 = new ThreadPoolExecutor(numberOfThread, numberOfThread, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), runnable -> {
                Thread thread = new Thread(runnable);
                thread.setName(THREADPOOL_PREFIX + counter.incrementAndGet());
                return thread;
            });
            tp1.prestartAllCoreThreads();
            tp = tp1;
            tpOwner = true;
        }

        this.threadPool = tp;
        this.threadPoolOwner = tpOwner;
        this.delayer = Executors.newScheduledThreadPool(1);

        poolMap = new AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool>() {
            @Override
            protected FixedChannelPool newPool(InetSocketAddress key) {
                workerGroup = new NioEventLoopGroup(numberOfThread, threadPool);

                final Bootstrap b = new Bootstrap();
                b.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true);
                return new FixedChannelPool(b.remoteAddress(key), new NettyAddressInitializer(), numberOfConnection);
            }
        };
    }

    @Override
    public CompletableFuture<Void> parseAddress(Map<String, String> requests, Consumer<Map<String, Map<String, String>>> callback,
                                                Function<Throwable, Void> exceptionHandler) {
        validateRequest(requests);
        CompletableFuture<Map<String, Map<String, String>>> future = new CompletableFuture<>();
        CompletableFuture<Void> f = future.acceptEither(timeoutAfter(CLIENT_TIMEOUT, TimeUnit.MILLISECONDS), callback).exceptionally(exceptionHandler);
        if (requests.isEmpty()) {
            future.complete(Collections.emptyMap());
        }
        logger.debug("Parse address for {} addresses", requests.size());
        UUID uuid = new UUID();
        logger.trace("AddressRequest id will be: {}", uuid);
        AddressDataModelProtos.AddressRequest.Builder requestBuilder = AddressDataModelProtos.AddressRequest.newBuilder();
        requestBuilder.setId(uuid.toString()).setType(AddressDataModelProtos.RequestType.PARSE);

        requests.forEach((id, address) -> {
            requestBuilder.addRequests(AddressDataModelProtos.AddressRequest.Request.newBuilder().setId(id).setAddress(address));
        });

        AddressDataModelProtos.AddressRequest request = requestBuilder.build();
        logger.trace("Sending address request with id: {} to server.", request.getId());
        send(uuid, request, future);
        return f;
    }

    @Override
    public CompletableFuture<Void> normalizeAddress(Map<String, String> requests, Consumer<Map<String, List<String>>> callback,
                                                    Function<Throwable, Void> exceptionHandler) {
        validateRequest(requests);
        CompletableFuture<Map<String, List<String>>> future = new CompletableFuture<>();
        CompletableFuture<Void> f = future.acceptEither(timeoutAfter(CLIENT_TIMEOUT, TimeUnit.MILLISECONDS), callback).exceptionally(exceptionHandler);
        if (requests.isEmpty()) {
            future.complete(Collections.emptyMap());
        }
        UUID uuid = new UUID();
        logger.trace("AddressRequest id will be: {}", uuid);
        AddressDataModelProtos.AddressRequest.Builder requestBuilder = AddressDataModelProtos.AddressRequest.newBuilder();
        requestBuilder.setId(uuid.toString()).setType(AddressDataModelProtos.RequestType.NORMALIZE);

        requests.forEach((id, address) -> {
            requestBuilder.addRequests(AddressDataModelProtos.AddressRequest.Request.newBuilder().setId(id).setAddress(address));
        });

        AddressDataModelProtos.AddressRequest request = requestBuilder.build();
        send(uuid, request, future);
        return f;
    }

    @Override
    public void close() {
        if (workerGroup != null) {
            try {
                workerGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS).await();
            } catch (InterruptedException e) {
                logger.warn("Exception when waiting for event loop group down.", e);
            } finally {
                workerGroup = null;
            }
        }
        if (threadPool != null && threadPoolOwner) {
            threadPool.shutdown();
        }
        delayer.shutdown();
    }

    public void addNewServer(InetSocketAddress socketAddress) {
        socketWriteLock.lock();
        try {
            if (!socketAddresses.contains(socketAddress)) {
                socketAddresses.add(socketAddress);
            }
        } finally {
            socketWriteLock.unlock();
        }
    }

    public void removeServer(InetSocketAddress socketAddress) {
        socketWriteLock.lock();
        try {
            socketAddresses.remove(socketAddress);
        } finally {
            socketWriteLock.unlock();
        }
    }

    private void send(final UUID uuid, final AddressDataModelProtos.AddressRequest request, CompletableFuture<? extends Map> future) {
        InetSocketAddress serverAddress = getNextSocketAddress();

        final FixedChannelPool pool = poolMap.get(serverAddress);
        Future<Channel> f = pool.acquire();
        f.addListener(fu -> {
            Future<Channel> f1 = (Future<Channel>) fu;
            if (f1.isSuccess()) {
                Channel ch = f1.getNow();
                ch.pipeline().get(NettyAddressHandler.class).addResponseFuture(ch.id(), uuid, future);
                ch.writeAndFlush(request).addListener(f2 -> pool.release(((ChannelFuture) f2).channel()));
            } else {
                logger.debug("Cannot get channel from pool.");
                future.completeExceptionally(new AddressException("Cannot get connection."));
            }
        });
    }

    private void validateRequest(Map<String, String> requests) {
        if (requests == null) {
            throw new NullPointerException("Request cannot be null");
        }
        if (requests.size() > MAX_ADDRESSES_PER_REQUEST) {
            throw new LimitExceededException(MAX_ADDRESSES_PER_REQUEST, requests.size());
        }
    }

    private void processExecutionException(ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new AddressException(cause);
            }
        } else {
            throw new AddressException(e);
        }
    }

    private InetSocketAddress getNextSocketAddress() {
        socketReadLock.lock();
        try {
            if (socketAddresses.isEmpty()) {
                throw new NoAvailabeServerException();
            }
            int idx;
            while ((idx = socketIndex.getAndIncrement()) >= socketAddresses.size()) {
                socketIndex.compareAndSet(idx + 1, 0);
            }
            return socketAddresses.get(idx);
        } finally {
            socketReadLock.unlock();
        }
    }

    private <T> CompletableFuture<T> timeoutAfter(long timeout, TimeUnit timeUnit) {
        CompletableFuture<T> result = new CompletableFuture<T>();
        delayer.schedule(() -> result.completeExceptionally(new TimeoutException()), timeout, timeUnit);
        return result;
    }

    @Override
    protected void finalize() {
        close();
    }
}
