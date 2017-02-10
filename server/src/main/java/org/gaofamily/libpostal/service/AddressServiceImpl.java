package org.gaofamily.libpostal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapzen.jpostal.AddressExpander;
import com.mapzen.jpostal.AddressParser;
import com.mapzen.jpostal.ExpanderOptions;
import com.mapzen.jpostal.ParsedComponent;
import org.gaofamily.libpostal.server.options.ExpanderOptionsJson;
import org.gaofamily.libpostal.utils.AddressHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
class AddressServiceImpl implements AddressService {
    private static final Logger logger = LoggerFactory.getLogger(AddressServiceImpl.class);

    private static ExpanderOptions expanderOptions;

    static {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = AddressServiceImpl.class.getResourceAsStream("/expand_options.json")) {
            ExpanderOptionsJson json = mapper.readValue(in, ExpanderOptionsJson.class);
            expanderOptions = AddressHelper.toExpanderOptions(json);
            logger.info("Expander option loaded.");
        } catch (IOException e) {
            logger.error("Cannot load libpostal expand options", e);
            System.exit(2);
        }
    }

    @Override
    public Map<String, Map<String, String>> parseAddress(Map<String, String> requests) {
        if (requests == null) {
            throw new NullPointerException("Addresses cannot be null.");
        }
        if (requests.isEmpty()) {
            return Collections.emptyMap();
        }
        logger.debug("Parsing {} addresses", requests.size());
        Map<String, Map<String, String>> results = new HashMap<>(requests.size());
        AddressParser parser = AddressParser.getInstance();
        requests.forEach((id, address) -> {
            logger.trace("Parsing address, id: {}, address: {}", id, address);
            ParsedComponent[] components = parser.parseAddress(address);
            logger.trace("Parse address result get {} components", components.length);
            Map<String, String> map = new LinkedHashMap(components.length);
            for (ParsedComponent component : components) {
                map.put(component.getLabel(), component.getValue());
            }
            results.put(id, map);
        });
        return results;
    }

    @Override
    public Map<String, List<String>> normalizeAddress(Map<String, String> requests) {
        if (requests == null) {
            throw new NullPointerException("Addresses cannot be null.");
        }
        if (requests.isEmpty()) {
            return Collections.emptyMap();
        }
        logger.debug("Normalizing {} addresses", requests.size());
        Map<String, List<String>> results = new HashMap<>(requests.size());
        AddressExpander expander = AddressExpander.getInstance();
        requests.forEach((id, address) -> {
            logger.trace("Normalizing address for id: {}, address: {}", id, address);
            String[] strs = expander.expandAddressWithOptions(address, expanderOptions);
            logger.trace("Normalize address result get {} components", strs.length);
            results.put(id, Arrays.asList(strs));
        });
        return results;
    }
}
