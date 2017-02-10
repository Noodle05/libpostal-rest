package org.gaofamily.libpostal.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Wei Gao
 * @since 8/10/16
 */
@XmlRootElement
public class AddressRequest implements Serializable {
    private static final long serialVersionUID = 6374261511294324979L;

    private String id;
    private String address;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "AddressRequest{" +
                "id='" + id + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
