package org.gaofamily.libpostal.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Wei Gao
 * @since 8/10/16
 */
@XmlRootElement
public class ParseResult implements Serializable {
    private static final long serialVersionUID = 1634998036764205873L;

    private String id;
    private Map<String, String> data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
