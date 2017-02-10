package org.gaofamily.libpostal.model.codec;

import org.gaofamily.libpostal.model.NormalizeResult;
import org.gaofamily.libpostal.model.ParseResult;
import org.gaofamily.libpostal.model.RequestType;
import org.gaofamily.libpostal.model.internal.BatchAddressResult;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * @author Wei Gao
 * @since 8/16/16
 */
public class AddressResultDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(AddressResultDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        logger.trace("Decoding address result...");
        UUID id = CodecHelper.readUUID(byteBuf);
        logger.debug("Decoding address result id: {}", id);
        int t = byteBuf.readByte();
        if (t < 0 || t >= RequestType.values().length) {
            throw new Exception("Invalid request type");
        }
        RequestType type = RequestType.values()[t];
        BatchAddressResult addressResult = new BatchAddressResult(id, type);
        t = byteBuf.readByte();
        if (t > 0) {
            t = byteBuf.readInt();
            List<ParseResult> parseResults = new ArrayList<>(t);
            for (int i = 0; i < t; i ++) {
                ParseResult parseResult = new ParseResult();
                String sid = CodecHelper.readString(byteBuf);
                parseResult.setId(sid);
                int len = byteBuf.readInt();
                if (len == 0) {
                    parseResult.setData(Collections.emptyMap());
                } else if (len > 0) {
                    Map<String, String> map = new LinkedHashMap<>(len);
                    for (int j = 0; j < len; j ++) {
                        map.put(CodecHelper.readString(byteBuf), CodecHelper.readString(byteBuf));
                    }
                    parseResult.setData(map);
                }
                parseResults.add(parseResult);
            }
            addressResult.setParseResults(parseResults);
        }
        t = byteBuf.readByte();
        if (t > 0) {
            t = byteBuf.readInt();
            List<NormalizeResult> normalizeResults = new ArrayList<>(t);
            for (int i = 0; i < t; i ++) {
                NormalizeResult normalizeResult = new NormalizeResult();
                String sid = CodecHelper.readString(byteBuf);
                normalizeResult.setId(sid);
                int len = byteBuf.readInt();
                if (len == 0) {
                    normalizeResult.setData(new String[]{});
                } else if (len > 0) {
                    String[] strs = new String[len];
                    for (int j = 0; j < len; j ++) {
                        strs[j] = CodecHelper.readString(byteBuf);
                    }
                    normalizeResult.setData(strs);
                }
                normalizeResults.add(normalizeResult);
            }
            addressResult.setNormalizeResults(normalizeResults);
        }
        logger.debug("Decode address result for id: {} done.", id);
        out.add(addressResult);
    }
}
