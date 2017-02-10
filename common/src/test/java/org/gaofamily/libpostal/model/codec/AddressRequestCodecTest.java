package org.gaofamily.libpostal.model.codec;

import org.gaofamily.libpostal.model.AddressRequest;
import org.gaofamily.libpostal.model.RequestType;
import org.gaofamily.libpostal.model.internal.BatchAddressRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Wei Gao
 * @since 8/19/16
 */
public class AddressRequestCodecTest {
    @Test(groups = {"unit"})
    public void testAddressRequestEncoder() throws Exception {
        UUID id = new UUID(0xfeefL, 0xdccdL);
        BatchAddressRequest request = new BatchAddressRequest(id, RequestType.PARSE);
        List<AddressRequest> requests = new ArrayList<>(1);
        AddressRequest r = new AddressRequest();
        r.setId("1");
        r.setAddress("Richtistrasse 3,WALLISELLEN,ZÜRICH,8304,CH");
        requests.add(r);
        request.setItems(requests);
        AddressRequestEncoder encoder = new AddressRequestEncoder();
        ByteBuf byteBuf = Unpooled.buffer();
        encoder.encode(null, request, byteBuf);
        byte[] bytes = byteBuf.array();
        Assert.assertNotNull(bytes);
        AddressRequestDecoder decoder = new AddressRequestDecoder();
        List<Object> res = new ArrayList<>(1);
        decoder.decode(null, byteBuf, res);
        Object obj = res.iterator().next();
        Assert.assertNotNull(obj);
    }
}
