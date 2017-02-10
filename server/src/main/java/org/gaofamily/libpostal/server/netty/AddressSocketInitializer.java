package org.gaofamily.libpostal.server.netty;

import org.gaofamily.libpostal.model.codec.AddressRequestDecoder;
import org.gaofamily.libpostal.model.codec.AddressResultEncoder;
import org.gaofamily.libpostal.model.codec.CodecHelper;
import org.gaofamily.libpostal.model.codec.DelimiterEncoder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public class AddressSocketInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("delimiterDecoder", new DelimiterBasedFrameDecoder(16384, Unpooled.wrappedBuffer(CodecHelper.delimiter)));
        pipeline.addLast("delimiterEncoder", new DelimiterEncoder());
        pipeline.addLast("addressRequestDecoder", new AddressRequestDecoder());
        pipeline.addLast("addressResultEncoder", new AddressResultEncoder());
        pipeline.addLast("logicHandler", new AddressRequestHandler());
    }
}
