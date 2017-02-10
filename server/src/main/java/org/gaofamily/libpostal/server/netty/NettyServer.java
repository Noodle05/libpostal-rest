package org.gaofamily.libpostal.server.netty;

import org.gaofamily.libpostal.server.AbstractServiceServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public class NettyServer extends AbstractServiceServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    public static final String TCP_PORT = "tcpPort";
    private static final String THREADPOOL_PREFIX = "Netty-";
    private final int numberOfThread;
    private final ExecutorService threadPool;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture future;

    public NettyServer(int numberOfThread) {
        this.numberOfThread = numberOfThread;
        AtomicInteger counter = new AtomicInteger(0);
        ThreadPoolExecutor tp = new ThreadPoolExecutor(numberOfThread,
                numberOfThread, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName(THREADPOOL_PREFIX + counter.incrementAndGet());
                    return thread;
                });
        tp.prestartAllCoreThreads();
        threadPool = tp;
    }

    @Override
    protected void internalStart() {
        int port = 0;
        String portStr = System.getProperty(TCP_PORT, "8090");
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            logger.error("Invalid TCP port value: {}", portStr, e);
            System.exit(1);
        }
        if (port <= 0 || port > 65536) {
            logger.error("Invalid TCP port value: {}", port);
            System.exit(1);
        }
        logger.info("Starting TCP API server on port {} ...", port);

        bossGroup = new NioEventLoopGroup(1, threadPool);
        workerGroup = new NioEventLoopGroup(numberOfThread - 1, threadPool);
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new AddressSocketInitializer())
                .option(ChannelOption.ALLOCATOR.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        try {
            future = b.bind(port).sync();
        } catch (InterruptedException e) {
            logger.error("Cannot bind to port: {}", port, e);
            throw new RuntimeException(e);
        }
        logger.info("TCP API server started.");
    }

    @Override
    protected void internalStop() {
        logger.info("Stopping TCP API server ...");
        List<Future<?>> futures = new ArrayList<>(3);
        if (future != null) {
            logger.info("Close channel");
            futures.add(future.channel().close());
        }
        if (workerGroup != null) {
            futures.add(workerGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS));
        }
        if (bossGroup != null) {
            futures.add(bossGroup.shutdownGracefully(0, 0, TimeUnit.SECONDS));
        }
        if (!futures.isEmpty()) {
            futures.forEach(future -> {
                try {
                    future.await();
                } catch (InterruptedException e) {
                    logger.warn("Exception when waiting for event loop group down.", e);
                }
            });
        }
        if (!threadPool.isShutdown()) {
            threadPool.shutdown();
        }
        logger.info("TCP API server stopped.");
    }
}
