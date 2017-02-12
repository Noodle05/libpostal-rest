package org.gaofamily.libpostal.server.netty;

import io.netty.handler.codec.protobuf.ProtobufDecoderNano;
import io.netty.handler.codec.protobuf.ProtobufEncoderNano;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.gaofamily.libpostal.model.nano.AddressDataModelProtos;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public class AddressSocketInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("Protobuf Frame Decoder", new ProtobufVarint32FrameDecoder());
        pipeline.addLast("Protobuf Decoder", new ProtobufDecoderNano(AddressDataModelProtos.AddressRequest.class));
        pipeline.addLast("Protobuf Length Encoder", new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast("Protobuf Encoder", new ProtobufEncoderNano());
        pipeline.addLast("logicHandler", new AddressRequestHandler());
    }
}
