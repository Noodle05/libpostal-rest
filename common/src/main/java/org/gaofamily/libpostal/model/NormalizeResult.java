package org.gaofamily.libpostal.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Wei Gao
 * @since 8/10/16
 */
@XmlRootElement
public class NormalizeResult implements Serializable {
    private static final long serialVersionUID = 3744096510981567558L;

    private String id;
    private String[] data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getData() {
        return data;
    }

    public void setData(String[] data) {
        this.data = data;
    }
}
