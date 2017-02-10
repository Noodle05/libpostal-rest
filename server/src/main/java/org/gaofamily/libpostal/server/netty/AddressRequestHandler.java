package org.gaofamily.libpostal.server.netty;

import org.gaofamily.libpostal.model.AddressDataModelProtos;
import org.gaofamily.libpostal.service.AddressService;
import org.gaofamily.libpostal.service.AddressServiceFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public class AddressRequestHandler extends ChannelInboundHandlerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(AddressRequestHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.debug("Chanel: \"{}\" get data {}", ctx.channel().id(), msg);
        try {
            AddressDataModelProtos.AddressRequest request = (AddressDataModelProtos.AddressRequest) msg;
            logger.debug("Got address request for id: {}", request.getId());
            AddressDataModelProtos.AddressResponse.Builder respBuilder = AddressDataModelProtos.AddressResponse.newBuilder();
            respBuilder.setId(request.getId());
            respBuilder.setType(request.getType());

            Map<String, String> requests = new HashMap<>(request.getRequestsCount());
            request.getRequestsList().forEach(req -> requests.put(req.getId(), req.getAddress()));
            AddressService addressService = AddressServiceFactory.getAddressService();
            switch (request.getType()) {
                case PARSE:
                    Map<String, Map<String, String>> pResults = addressService.parseAddress(requests);
                    logger.trace("Parse address done.");
                    pResults.forEach((id, result) -> {
                        AddressDataModelProtos.AddressResponse.ParseResponse.Builder prBuilder = AddressDataModelProtos.AddressResponse.ParseResponse.newBuilder();
                        prBuilder.setId(id);
                        result.forEach((key, value) -> {
                            prBuilder.putData(key, value);
                        });
                        respBuilder.addParseResult(prBuilder);
                    });
                    break;
                case NORMALIZE:
                    Map<String, List<String>> nResults = addressService.normalizeAddress(requests);
                    logger.trace("Normalize address done.");
                    nResults.forEach((id, result) -> {
                        respBuilder.addNormalizeResult(AddressDataModelProtos.AddressResponse.NormalizeResponse.newBuilder()
                                .setId(id)
                                .addAllData(result));
                    });
                    break;
            }
            ctx.writeAndFlush(respBuilder.build());
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn("Exception happened on this channel: {}", ctx, cause);
        ctx.close();
    }
}
