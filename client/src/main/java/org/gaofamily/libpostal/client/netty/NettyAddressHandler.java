package org.gaofamily.libpostal.client.netty;

import org.gaofamily.libpostal.model.internal.BatchAddressResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ClosedChannelException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by wgao on 8/18/16.
 */
class NettyAddressHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyAddressHandler.class);

    private final ConcurrentMap<ChannelId, ConcurrentMap<UUID, ResponseFuture>> responses;

    NettyAddressHandler() {
        responses = new ConcurrentHashMap<>();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            logger.trace("Get response: ", msg);
            ChannelId cid = ctx.channel().id();
            ConcurrentMap<UUID, ResponseFuture> reps = responses.get(cid);
            if (reps != null) {
                BatchAddressResult result = (BatchAddressResult) msg;
                ResponseFuture future = reps.get(result.getId());
                if (future != null) {
                    future.setResult(result);
                }
            } else {
                logger.error("This channel has not response map yet!");
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Exception happened on this channel: {}", ctx, cause);

        cleanUpChannel(ctx, cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("Channel {} inactived.", ctx.channel().id());
        cleanUpChannel(ctx, new ClosedChannelException());
        super.channelInactive(ctx);
    }

    private void cleanUpChannel(ChannelHandlerContext ctx, Throwable cause) {
        ChannelId cid = ctx.channel().id();
        logger.trace("Cleaning up response map for channel: {}", cid);
        ConcurrentMap<UUID, ResponseFuture> reps = responses.remove(cid);

        if (reps != null) {
            reps.forEach((id, future) -> {
                future.setFailure(cause);
            });
        }
    }

    public void addResponseFuture(ChannelId cid, UUID id, ResponseFuture future) {
        ConcurrentMap<UUID, ResponseFuture> reps = responses.get(cid);
        if (reps == null) {
            reps = new ConcurrentHashMap<>();
            ConcurrentMap<UUID, ResponseFuture> tmp = responses.putIfAbsent(cid, reps);
            if (tmp != null) {
                reps = tmp;
            }
        }
        reps.put(id, future);
    }
}