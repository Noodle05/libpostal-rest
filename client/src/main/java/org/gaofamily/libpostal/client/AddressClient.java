package org.gaofamily.libpostal.client;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public interface AddressClient extends Closeable {
    Map<String, Map<String, String>> parseAddress(Map<String, String> requests);

    Map<String, List<String>> normalizeAddress(Map<String, String> requests);
}
