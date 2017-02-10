package org.gaofamily.libpostal.model.codec;

import org.gaofamily.libpostal.model.internal.BatchAddressResult;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public class AddressResultEncoder extends MessageToByteEncoder<BatchAddressResult> {
    private static final Logger logger = LoggerFactory.getLogger(AddressResultEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, BatchAddressResult msg, ByteBuf out) throws Exception {
        if (msg == null) {
            throw new NullPointerException();
        }
        if (msg.getType() == null) {
            throw new NullPointerException("request type is null");
        }
        if (msg.getParseResults() == null && msg.getNormalizeResults() == null) {
            throw new NullPointerException("Result items is null");
        }
        logger.debug("Encoding address result with id: {}", msg.getId());
        CodecHelper.writeUUID(out, msg.getId());
        out.writeByte(msg.getType().ordinal());
        if (msg.getParseResults() != null) {
            out.writeByte(1);
            out.writeInt(msg.getParseResults().size());
            msg.getParseResults().forEach(parseResult -> {
                CodecHelper.writeString(out, parseResult.getId());
                if (parseResult.getData() == null) {
                    out.writeInt(-1);
                } else {
                    out.writeInt(parseResult.getData().size());
                    parseResult.getData().forEach((key, value) -> {
                        CodecHelper.writeString(out, key);
                        CodecHelper.writeString(out, value);
                    });
                }
            });
        } else {
            out.writeByte(0);
        }
        if (msg.getNormalizeResults() != null) {
            out.writeByte(1);
            out.writeInt(msg.getNormalizeResults().size());
            msg.getNormalizeResults().forEach(normalizeResult -> {
                CodecHelper.writeString(out, normalizeResult.getId());
                if (normalizeResult.getData() == null) {
                    out.writeInt(-1);
                } else {
                    out.writeInt(normalizeResult.getData().length);
                    for (String str : normalizeResult.getData()) {
                        CodecHelper.writeString(out, str);
                    }
                }
            });
        } else {
            out.writeByte(0);
        }
        logger.debug("Encode for address result with id: {} done.", msg.getId());
    }
}
