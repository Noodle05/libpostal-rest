package org.gaofamily.libpostal.server.rest;

import org.gaofamily.libpostal.service.AddressService;
import org.gaofamily.libpostal.service.AddressServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * @author Wei Gao
 * @since 2/10/17
 */
@Path("parse")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AddressParseHandler {
    private static final Logger logger = LoggerFactory.getLogger(AddressParseHandler.class);

    @POST
    public Map<String, Map<String, String>> parseAddress(Map<String, String> requests) {
        logger.debug("Get parse address request: {}", requests);
        AddressService addressService = AddressServiceFactory.getAddressService();
        return addressService.parseAddress(requests);
    }
}
