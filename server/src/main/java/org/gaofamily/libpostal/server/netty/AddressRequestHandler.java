package org.gaofamily.libpostal.server.netty;

import org.gaofamily.libpostal.model.nano.AddressDataModelProtos;
import org.gaofamily.libpostal.service.AddressService;
import org.gaofamily.libpostal.service.AddressServiceFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public class AddressRequestHandler extends ChannelInboundHandlerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(AddressRequestHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.debug("Chanel: \"{}\" get data {}", ctx.channel().id(), msg);
        try {
            AddressDataModelProtos.AddressRequest request = (AddressDataModelProtos.AddressRequest) msg;
            logger.debug("Got address request for id: {}", request.id);
            AddressDataModelProtos.AddressResponse response = new AddressDataModelProtos.AddressResponse();
            response.id = request.id;
            response.type = request.type;

            Map<String, String> requests = new HashMap<>(request.requests.length);
            for (AddressDataModelProtos.AddressRequest.Request req : request.requests) {
                requests.put(req.id, req.address);
            }
            AddressService addressService = AddressServiceFactory.getAddressService();
            switch (request.type) {
                case AddressDataModelProtos.PARSE:
                    Map<String, Map<String, String>> pResults = addressService.parseAddress(requests);
                    logger.trace("Parse address done.");
                    Collection<AddressDataModelProtos.AddressResponse.ParseResponse> prs = new ArrayList<>(pResults.size());
                    pResults.forEach((id, result) -> {
                        AddressDataModelProtos.AddressResponse.ParseResponse pr = new AddressDataModelProtos.AddressResponse.ParseResponse();
                        pr.id = id;
                        Collection<AddressDataModelProtos.AddressResponse.ParseResponse.DataEntry> ds = new ArrayList<>();
                        result.forEach((key, value) -> {
                            AddressDataModelProtos.AddressResponse.ParseResponse.DataEntry de = new AddressDataModelProtos.AddressResponse.ParseResponse.DataEntry();
                            de.key = key;
                            de.value = value;
                            ds.add(de);
                        });
                        pr.data = ds.toArray(new AddressDataModelProtos.AddressResponse.ParseResponse.DataEntry[ds.size()]);
                        prs.add(pr);
                    });
                    response.parseResult = prs.toArray(new AddressDataModelProtos.AddressResponse.ParseResponse[prs.size()]);
                    break;
                case AddressDataModelProtos.NORMALIZE:
                    Map<String, List<String>> nResults = addressService.normalizeAddress(requests);
                    logger.trace("Normalize address done.");
                    Collection<AddressDataModelProtos.AddressResponse.NormalizeResponse> ns = new ArrayList<>(nResults.size());
                    nResults.forEach((id, result) -> {
                        AddressDataModelProtos.AddressResponse.NormalizeResponse nr = new AddressDataModelProtos.AddressResponse.NormalizeResponse();
                        nr.id = id;
                        nr.data = result.toArray(new String[result.size()]);
                        ns.add(nr);
                    });
                    response.normalizeResult = ns.toArray(new AddressDataModelProtos.AddressResponse.NormalizeResponse[ns.size()]);
                    break;
            }
            ctx.writeAndFlush(response);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn("Exception happened on this channel: {}", ctx, cause);
        ctx.close();
    }
}
