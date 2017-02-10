package org.gaofamily.libpostal.model.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author Wei Gao
 * @since 8/19/16
 */
public class DelimiterEncoder extends MessageToByteEncoder<byte[]> {
    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] msg, ByteBuf out) throws Exception {
        out.writeBytes(msg);
    }
}
