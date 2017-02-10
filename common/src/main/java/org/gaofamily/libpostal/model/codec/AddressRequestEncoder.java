package org.gaofamily.libpostal.model.codec;

import org.gaofamily.libpostal.model.internal.BatchAddressRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public class AddressRequestEncoder extends MessageToByteEncoder<BatchAddressRequest> {
    private static final Logger logger = LoggerFactory.getLogger(AddressRequestEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, BatchAddressRequest msg, ByteBuf out) throws Exception {
        if (msg == null) {
            throw new NullPointerException();
        }
        if (msg.getType() == null) {
            throw new NullPointerException("request type is null");
        }
        if (msg.getItems() == null) {
            throw new NullPointerException("request items isnull");
        }
        logger.debug("Encoding address request for id: {}", msg.getId());
        CodecHelper.writeUUID(out, msg.getId());
        out.writeByte(msg.getType().ordinal());

        out.writeInt(msg.getItems().size());
        msg.getItems().forEach(item -> {
            CodecHelper.writeString(out, item.getId());
            CodecHelper.writeString(out, item.getAddress());
        });
        logger.trace("Encode address request for id: {} done.", msg.getId());
    }
}
