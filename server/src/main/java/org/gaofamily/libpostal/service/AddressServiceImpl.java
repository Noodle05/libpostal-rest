package org.gaofamily.libpostal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapzen.jpostal.AddressExpander;
import com.mapzen.jpostal.AddressParser;
import com.mapzen.jpostal.ExpanderOptions;
import com.mapzen.jpostal.ParsedComponent;
import org.gaofamily.libpostal.model.AddressRequest;
import org.gaofamily.libpostal.model.NormalizeResult;
import org.gaofamily.libpostal.model.ParseResult;
import org.gaofamily.libpostal.server.options.ExpanderOptionsJson;
import org.gaofamily.libpostal.server.rest.AddressNormalizeHandler;
import org.gaofamily.libpostal.utils.AddressHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
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
        try (InputStream in = AddressNormalizeHandler.class.getResourceAsStream("/expand_options.json")) {
            ExpanderOptionsJson json = mapper.readValue(in, ExpanderOptionsJson.class);
            expanderOptions = AddressHelper.toExpanderOptions(json);
            logger.info("Expander option loaded.");
        } catch (IOException e) {
            logger.error("Cannot load libpostal expand options", e);
            System.exit(2);
        }
    }

    @Override
    public List<ParseResult> parseAddress(List<AddressRequest> requests) {
        if (requests == null) {
            throw new NullPointerException("Addresses cannot be null.");
        }
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }
        logger.debug("Parsing {} addresses", requests.size());
        List<ParseResult> results = new ArrayList<>(requests.size());
        AddressParser parser = AddressParser.getInstance();
        requests.forEach((request) -> {
            logger.trace("Parsing address, request: {}", request);
            ParsedComponent[] components = parser.parseAddress(request.getAddress());
            logger.trace("Parse address result get {} components", components.length);
            Map<String, String> map = new LinkedHashMap(components.length);
            for (ParsedComponent component : components) {
                map.put(component.getLabel(), component.getValue());
            }
            ParseResult result = new ParseResult();
            result.setId(request.getId());
            result.setData(map);
            results.add(result);
        });
        return results;
    }

    @Override
    public List<NormalizeResult> normalizeAddress(List<AddressRequest> requests) {
        if (requests == null) {
            throw new NullPointerException("Addresses cannot be null.");
        }
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }
        logger.debug("Normalizing {} addreses", requests.size());
        List<NormalizeResult> results = new ArrayList<>(requests.size());
        AddressExpander expander = AddressExpander.getInstance();
        requests.forEach((request) -> {
            logger.trace("Normalizing address for request: {}", request);
            String[] strs = expander.expandAddressWithOptions(request.getAddress(), expanderOptions);
            logger.trace("Normalize address result get {} components", strs.length);
            NormalizeResult result = new NormalizeResult();
            result.setId(request.getId());
            result.setData(strs);
            results.add(result);
        });
        return results;
    }
}
