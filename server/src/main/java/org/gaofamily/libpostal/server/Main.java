package org.gaofamily.libpostal.server;

import com.mapzen.jpostal.AddressExpander;
import com.mapzen.jpostal.AddressParser;
import org.gaofamily.libpostal.server.netty.NettyServer;
import org.gaofamily.libpostal.server.rest.NettyRestServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wei Gao
 * @since 8/10/16
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        logger.info("Starting libpostal web service...");
        logger.info("Initialize libpostal");
        // AddressParser will need to load JNI library, so init it before
        // we start to make sure it will fail if load JNI library failed.
        AddressParser.getInstance();
        AddressExpander.getInstance();

        int numberOfThreads = Runtime.getRuntime().availableProcessors() * 2;

        logger.info("Initial jetty server.");
        final List<ServiceServer> servers = new ArrayList<>(2);
        ServiceServer resetServer = new NettyRestServer();
//        ServiceServer jettyServer = new JettyServer(numberOfThreads);
        servers.add(resetServer);
        ServiceServer tcpServer = new NettyServer(numberOfThreads);
        servers.add(tcpServer);

        logger.info("Starting servers");
        servers.forEach(server -> server.start());

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("Stopping servers");
                servers.forEach(server -> {
                    server.stop();
                });
            }
        });
        logger.info("Exit main thread");
    }
}
