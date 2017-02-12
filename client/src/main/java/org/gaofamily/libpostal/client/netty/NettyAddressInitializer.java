package org.gaofamily.libpostal.client.netty;

import io.netty.handler.codec.protobuf.ProtobufDecoderNano;
import io.netty.handler.codec.protobuf.ProtobufEncoderNano;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.gaofamily.libpostal.model.nano.AddressDataModelProtos;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.AbstractChannelPoolHandler;

/**
 * Created by wgao on 8/18/16.
 */
class NettyAddressInitializer extends AbstractChannelPoolHandler {
    @Override
    public void channelCreated(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("Protobuf Frame Decoder", new ProtobufVarint32FrameDecoder());
        pipeline.addLast("Protobuf Decoder", new ProtobufDecoderNano(AddressDataModelProtos.AddressResponse.class));
        pipeline.addLast("Protobuf Length Encoder", new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast("Protobuf Encoder", new ProtobufEncoderNano());
        pipeline.addLast("logicHandler", new NettyAddressHandler());
    }
}