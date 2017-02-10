package org.gaofamily.libpostal.server.rest;

import org.gaofamily.libpostal.server.AbstractServiceServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.glassfish.jersey.netty.httpserver.NettyHttpContainerProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * @author Wei Gao
 * @since 10/28/16
 */
public class NettyRestServer extends AbstractServiceServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyRestServer.class);

    public static final String HTTP_PORT = "httpPort";

    private Channel server;

    @Override
    protected void internalStart() {
        int port = 0;
        String portStr = System.getProperty(HTTP_PORT, "8080");
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            logger.error("Invalid Rest API port value: " + portStr, e);
            System.exit(1);
        }
        if (port <= 0 || port > 65536) {
            logger.error("Invalid Rest API port value: {}", port);
            System.exit(1);
        }
        logger.info("Starting Rest API server on port: {}", port);
        URI baseUri = UriBuilder.fromUri("http://localhost/").port(port).build();
        ResourceConfig config = new ResourceConfig(AddressParseHandler.class, AddressNormalizeHandler.class);
        server = NettyHttpContainerProvider.createServer(baseUri, config, false);
        logger.info("Rest API server started");
    }

    @Override
    protected void internalStop() {
        logger.info("Stopping Rest API server ...");
        ChannelFuture future = server.close();
        try {
            future.await();
        } catch (InterruptedException e) {
            logger.warn("Exception when waiting for event loop group down.", e);
        }
        logger.info("Rest API server stopped.");
    }
}
