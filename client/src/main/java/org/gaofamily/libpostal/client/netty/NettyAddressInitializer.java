package org.gaofamily.libpostal.client.netty;

import org.gaofamily.libpostal.model.codec.AddressRequestEncoder;
import org.gaofamily.libpostal.model.codec.AddressResultDecoder;
import org.gaofamily.libpostal.model.codec.CodecHelper;
import org.gaofamily.libpostal.model.codec.DelimiterEncoder;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

/**
 * Created by wgao on 8/18/16.
 */
class NettyAddressInitializer extends AbstractChannelPoolHandler {
    @Override
    public void channelCreated(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("delimiterDecoder", new DelimiterBasedFrameDecoder(16384, Unpooled.wrappedBuffer(CodecHelper.delimiter)));
        pipeline.addLast("delimiterEncoder", new DelimiterEncoder());
        pipeline.addLast("addressRequestEncoder", new AddressRequestEncoder());
        pipeline.addLast("addressResultDecoder", new AddressResultDecoder());
        pipeline.addLast("logicHandler", new NettyAddressHandler());
    }
}