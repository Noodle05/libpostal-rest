package org.gaofamily.libpostal.server.netty;

import org.gaofamily.libpostal.model.codec.CodecHelper;
import org.gaofamily.libpostal.model.internal.BatchAddressRequest;
import org.gaofamily.libpostal.model.internal.BatchAddressResult;
import org.gaofamily.libpostal.model.NormalizeResult;
import org.gaofamily.libpostal.model.ParseResult;
import org.gaofamily.libpostal.service.AddressService;
import org.gaofamily.libpostal.service.AddressServiceFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
            BatchAddressRequest request = (BatchAddressRequest) msg;
            logger.debug("Got address request for id: {}", request.getId());
            BatchAddressResult result = new BatchAddressResult(request.getId(), request.getType());
            AddressService addressService = AddressServiceFactory.getAddressService();
            switch (request.getType()) {
                case PARSE:
                    List<ParseResult> parseResults = addressService.parseAddress(request.getItems());
                    logger.trace("Parse address done.");
                    result.setParseResults(parseResults);
                    break;
                case NORMALIZE:
                    List<NormalizeResult> normalizeResults = addressService.normalizeAddress(request.getItems());
                    logger.trace("Normalize address done.");
                    result.setNormalizeResults(normalizeResults);
                    break;
            }
            ctx.write(result);
            ctx.writeAndFlush(CodecHelper.delimiter);
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
