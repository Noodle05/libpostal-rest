package org.gaofamily.libpostal.model.internal;

import org.gaofamily.libpostal.model.AddressRequest;
import org.gaofamily.libpostal.model.RequestType;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public class BatchAddressRequest implements Serializable {
    private static final long serialVersionUID = -6733034462473227164L;

    private final UUID id;
    private final RequestType type;
    private List<AddressRequest> items;

    public BatchAddressRequest(UUID id, RequestType type) {
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

    public List<AddressRequest> getItems() {
        return items;
    }

    public void setItems(List<AddressRequest> items) {
        this.items = items;
    }
}
