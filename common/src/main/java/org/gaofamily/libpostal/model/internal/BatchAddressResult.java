package org.gaofamily.libpostal.model.internal;

import org.gaofamily.libpostal.model.NormalizeResult;
import org.gaofamily.libpostal.model.ParseResult;
import org.gaofamily.libpostal.model.RequestType;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public class BatchAddressResult implements Serializable {
    private static final long serialVersionUID = 7922716840521211810L;

    private final UUID id;
    private final RequestType type;
    private List<ParseResult> parseResults;
    private List<NormalizeResult> normalizeResults;

    public BatchAddressResult(UUID id, RequestType type) {
        assert id != null;
        assert type != null;
        this.id = id;
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public RequestType getType() {
        return type;
    }

    public List<ParseResult> getParseResults() {
        return parseResults;
    }

    public void setParseResults(List<ParseResult> parseResults) {
        this.parseResults = parseResults;
    }

    public List<NormalizeResult> getNormalizeResults() {
        return normalizeResults;
    }

    public void setNormalizeResults(List<NormalizeResult> normalizeResults) {
        this.normalizeResults = normalizeResults;
    }
}
