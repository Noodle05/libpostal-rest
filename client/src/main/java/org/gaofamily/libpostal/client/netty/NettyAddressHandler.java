package org.gaofamily.libpostal.client.netty;

import org.gaofamily.libpostal.client.utils.UUIDHelper;
import org.gaofamily.libpostal.model.nano.AddressDataModelProtos;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ClosedChannelException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by wgao on 8/18/16.
 */
class NettyAddressHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyAddressHandler.class);

    private final ConcurrentMap<ChannelId, ConcurrentMap<UUID, CompletableFuture<? extends Map>>> responses;

    NettyAddressHandler() {
        responses = new ConcurrentHashMap<>();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            logger.trace("Get response: {}", msg);
            ChannelId cid = ctx.channel().id();
            ConcurrentMap<UUID, CompletableFuture<? extends Map>> reps = responses.get(cid);
            if (reps != null) {
                AddressDataModelProtos.AddressResponse result = (AddressDataModelProtos.AddressResponse) msg;
                UUID uuid = UUIDHelper.fromBytes(result.id);
                CompletableFuture<? extends Map> future = reps.remove(uuid);
                if (future != null) {
                    switch (result.type) {
                        case AddressDataModelProtos.PARSE:
                            Map<String, Map<String, String>> pRes = new HashMap<>();
                            for (AddressDataModelProtos.AddressResponse.ParseResponse pr : result.parseResult) {
                                Map<String, String> pre = new HashMap<>(pr.data.length);
                                for (AddressDataModelProtos.AddressResponse.ParseResponse.DataEntry de : pr.data) {
                                    pre.put(de.key, de.value);
                                }
                                pRes.put(pr.id, pre);
                            }
                            final CompletableFuture<Map<String, Map<String, String>>> pf = (CompletableFuture<Map<String, Map<String, String>>>) future;
                            pf.complete(pRes);
                            break;
                        case AddressDataModelProtos.NORMALIZE:
                            Map<String, List<String>> rRes = new HashMap<>(result.normalizeResult.length);
                            for (AddressDataModelProtos.AddressResponse.NormalizeResponse nr : result.normalizeResult) {
                                rRes.put(nr.id, Arrays.asList(nr.data));
                            }
                            final CompletableFuture<Map<String, List<String>>> rf = (CompletableFuture<Map<String, List<String>>>) future;
                            rf.complete(rRes);
                            break;
                    }
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
        ConcurrentMap<UUID, CompletableFuture<? extends Map>> reps = responses.remove(cid);

        if (reps != null) {
            reps.forEach((id, future) -> future.completeExceptionally(cause));
        }
    }

    void addResponseFuture(ChannelId cid, UUID id, CompletableFuture<? extends Map> future) {
        ConcurrentMap<UUID, CompletableFuture<? extends Map>> reps = responses.get(cid);
        if (reps == null) {
            reps = new ConcurrentHashMap<>();
            ConcurrentMap<UUID, CompletableFuture<? extends Map>> tmp = responses.putIfAbsent(cid, reps);
            if (tmp != null) {
                reps = tmp;
            }
        }

        reps.put(id, future);
    }
}