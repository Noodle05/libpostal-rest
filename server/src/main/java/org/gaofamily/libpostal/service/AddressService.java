package org.gaofamily.libpostal.service;

import org.gaofamily.libpostal.model.AddressRequest;
import org.gaofamily.libpostal.model.NormalizeResult;
import org.gaofamily.libpostal.model.ParseResult;

import java.util.List;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public interface AddressService {
    List<ParseResult> parseAddress(List<AddressRequest> requests);

    List<NormalizeResult> normalizeAddress(List<AddressRequest> requests);
}
