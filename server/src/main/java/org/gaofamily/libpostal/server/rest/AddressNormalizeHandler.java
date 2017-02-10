package org.gaofamily.libpostal.server.rest;

import org.gaofamily.libpostal.model.AddressRequest;
import org.gaofamily.libpostal.model.NormalizeResult;
import org.gaofamily.libpostal.service.AddressService;
import org.gaofamily.libpostal.service.AddressServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author Wei Gao
 * @since 8/10/16
 */
@Path("normalize")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AddressNormalizeHandler {
    private static final Logger logger = LoggerFactory.getLogger(AddressNormalizeHandler.class);

    @POST
    public List<NormalizeResult> normalizeAddress(List<AddressRequest> requests) {
        logger.debug("Get normalize address request: {}", requests);
        AddressService addressService = AddressServiceFactory.getAddressService();

        return addressService.normalizeAddress(requests);
    }
}
