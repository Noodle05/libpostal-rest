package org.gaofamily.libpostal.server.rest;

import org.gaofamily.libpostal.model.AddressRequest;
import org.gaofamily.libpostal.model.ParseResult;
import org.gaofamily.libpostal.service.AddressService;
import org.gaofamily.libpostal.service.AddressServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * @author Wei Gao
 * @since 8/10/16
 */
@Path("parse")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AddressParseHandler {
    private static final Logger logger = LoggerFactory.getLogger(AddressParseHandler.class);

    @POST
    public List<ParseResult> parseAddress(List<AddressRequest> requests) {
        logger.debug("Get parse address request: {}", requests);
        AddressService addressService = AddressServiceFactory.getAddressService();
        return addressService.parseAddress(requests);
    }
}
