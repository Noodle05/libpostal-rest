package org.gaofamily.libpostal.service;

import java.util.List;
import java.util.Map;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public interface AddressService {
    Map<String, Map<String, String>> parseAddress(Map<String, String> requests);

    Map<String, List<String>> normalizeAddress(Map<String, String> requests);
}
