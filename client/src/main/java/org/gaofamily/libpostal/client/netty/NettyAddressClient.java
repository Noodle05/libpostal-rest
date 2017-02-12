package org.gaofamily.libpostal.client.netty;

import org.gaofamily.libpostal.client.AddressClient;
import org.gaofamily.libpostal.client.AddressException;
import org.gaofamily.libpostal.client.LimitExceededException;
import org.gaofamily.libpostal.client.NoAvailabeServerException;
import org.gaofamily.libpostal.client.utils.UUIDHelper;
import org.gaofamily.libpostal.model.nano.AddressDataModelProtos;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public class NettyAddressClient implements AddressClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyAddressClient.class);
    private static final String THREADPOOL_PREFIX = "NettyClient-";
    private static final int DEFAULT_CONNECTION_NUMBER = 1;
    private static final int DEFAULT_THREAD_NUMBER = 0;
    private static final int MAX_ADDRESSES_PER_REQUEST = 100;
    private static final long CLIENT_TIMEOUT = 3000L;

    private final ExecutorService threadPool;
    private final ScheduledExecutorService delayer;
    private final ChannelPoolMap<InetSocketAddress, FixedChannelPool> poolMap;
    private EventLoopGroup workerGroup;
    private final List<InetSocketAddress> socketAddresses;
    private final Lock socketReadLock;
    private final Lock socketWriteLock;
    private final AtomicInteger socketIndex;

    public NettyAddressClient(String host, int port) throws InterruptedException, UnknownHostException {
        this(DEFAULT_CONNECTION_NUMBER, DEFAULT_THREAD_NUMBER, new InetSocketAddress(host, port));
    }

    public NettyAddressClient(String host, int port, int numberOfConnection) throws InterruptedException, UnknownHostException {
        this(numberOfConnection, DEFAULT_THREAD_NUMBER, new InetSocketAddress(host, port));
    }

    public NettyAddressClient(String host, int port, int numberOfConnection, int numberOfThread) throws InterruptedException, UnknownHostException {
        this(numberOfConnection, numberOfThread, new InetSocketAddress(host, port));
    }

    public NettyAddressClient(InetSocketAddress... addresses) throws InterruptedException, UnknownHostException {
        this(DEFAULT_CONNECTION_NUMBER, DEFAULT_THREAD_NUMBER, addresses);
    }

    public NettyAddressClient(int numberOfConnection, int numberOfThread, InetSocketAddress... serverAddresses) throws InterruptedException, UnknownHostException {
        assert serverAddresses != null;
        assert serverAddresses.length > 0;
        assert numberOfConnection > 0;
        this.socketAddresses = Arrays.asList(serverAddresses);
        ReadWriteLock rwl = new ReentrantReadWriteLock();
        socketReadLock = rwl.readLock();
        socketWriteLock = rwl.writeLock();
        socketIndex = new AtomicInteger(0);

        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1, run -> {
            Thread thread = new Thread(run);
            thread.setName("AsyncEventDelayerThread");
            return thread;
        });
        scheduler.prestartAllCoreThreads();
        this.delayer = scheduler;
        final AtomicInteger counter = new AtomicInteger(0);
        ExecutorService tp;
        ThreadFactory tf = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(THREADPOOL_PREFIX + counter.incrementAndGet());
            return thread;
        };

        if (numberOfThread > 0) {
            tp = Executors.newFixedThreadPool(numberOfThread, tf);
        } else {
            tp = Executors.newCachedThreadPool(tf);
        }
        this.threadPool = tp;

        poolMap = new AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool>() {
            @Override
            protected FixedChannelPool newPool(InetSocketAddress key) {
                workerGroup = new NioEventLoopGroup(numberOfThread, tp);

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
        UUID uuid = UUID.randomUUID();
        logger.trace("AddressRequest id will be: {}", uuid);

        AddressDataModelProtos.AddressRequest request = generateAddressRequest(requests, uuid, AddressDataModelProtos.PARSE);

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
        UUID uuid = UUID.randomUUID();
        logger.trace("AddressRequest id will be: {}", uuid);

        AddressDataModelProtos.AddressRequest request = generateAddressRequest(requests, uuid, AddressDataModelProtos.NORMALIZE);

        send(uuid, request, future);
        return f;
    }

    @Override
    public void close() {
        if (workerGroup != null) {
            try {
                workerGroup.shutdownGracefully(0, 1, TimeUnit.SECONDS).await();
            } catch (InterruptedException e) {
                logger.warn("Exception when waiting for event loop group down.", e);
            } finally {
                workerGroup = null;
            }
        }
        threadPool.shutdown();
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
        logger.trace("Calling send request for uuid: {}", uuid);
        InetSocketAddress serverAddress = getNextSocketAddress();

        final FixedChannelPool pool = poolMap.get(serverAddress);
        Future<Channel> f = pool.acquire();
        f.addListener(fu -> {
            Future<Channel> f1 = (Future<Channel>) fu;
            if (f1.isSuccess()) {
                final Channel ch = f1.getNow();
                ch.pipeline().get(NettyAddressHandler.class).addResponseFuture(ch.id(), uuid, future);
                ch.writeAndFlush(request).addListener(f2 -> pool.release(((ChannelFuture) f2).channel()));
            } else {
                logger.debug("Cannot get channel from pool.");
                future.completeExceptionally(new AddressException("Cannot get connection."));
            }
        });
    }

    private AddressDataModelProtos.AddressRequest generateAddressRequest(Map<String, String> requests, UUID uuid, int operation) {
        AddressDataModelProtos.AddressRequest request = new AddressDataModelProtos.AddressRequest();
        request.id = UUIDHelper.toBytes(uuid);
        request.type = AddressDataModelProtos.PARSE;

        Collection<AddressDataModelProtos.AddressRequest.Request> rs = new ArrayList<>(requests.size());
        requests.forEach((id, address) -> {
            AddressDataModelProtos.AddressRequest.Request r = new AddressDataModelProtos.AddressRequest.Request();
            r.id = id;
            r.address = address;
            rs.add(r);
        });
        request.requests = rs.toArray(new AddressDataModelProtos.AddressRequest.Request[rs.size()]);
        return request;
    }

    private void validateRequest(Map<String, String> requests) {
        if (requests == null) {
            throw new NullPointerException("Request cannot be null");
        }
        if (requests.size() > MAX_ADDRESSES_PER_REQUEST) {
            throw new LimitExceededException(MAX_ADDRESSES_PER_REQUEST, requests.size());
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
