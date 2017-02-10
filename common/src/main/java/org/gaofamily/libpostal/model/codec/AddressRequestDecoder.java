package org.gaofamily.libpostal.model.codec;

import org.gaofamily.libpostal.model.AddressRequest;
import org.gaofamily.libpostal.model.RequestType;
import org.gaofamily.libpostal.model.internal.BatchAddressRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public class AddressRequestDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(AddressRequestDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        logger.trace("Decoding message ...");
        UUID id = CodecHelper.readUUID(byteBuf);
        logger.debug("Decoding address request with id: {}", id);
        int t = byteBuf.readByte();
        if (t < 0 || t >= RequestType.values().length) {
            throw new Exception("Invalid request type");
        }
        RequestType type = RequestType.values()[t];
        t = byteBuf.readInt();
        if (t < 0) {
            throw new Exception("Invalid request length");
        }
        List<AddressRequest> items;
        if (t == 0) {
            items = Collections.emptyList();
        } else {
            items = new ArrayList<>(t);
        }
        for (int i = 0; i < t; i++) {
            String sid = CodecHelper.readString(byteBuf);
            String address = CodecHelper.readString(byteBuf);
            AddressRequest item = new AddressRequest();
            item.setId(sid);
            item.setAddress(address);
            items.add(item);
        }
        BatchAddressRequest request = new BatchAddressRequest(id, type);
        request.setItems(items);
        logger.trace("Decode address request for id: {} done.", id);
        list.add(request);
    }
}
