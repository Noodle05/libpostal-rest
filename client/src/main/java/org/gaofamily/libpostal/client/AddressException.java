package org.gaofamily.libpostal.client;

/**
 * @author Wei Gao
 * @since 8/17/16
 */
public class AddressException extends RuntimeException {
    private static final long serialVersionUID = -4757874732881067362L;

    public AddressException() {
        super();
    }

    public AddressException(String msg) {
        super(msg);
    }

    public AddressException(Throwable cause) {
        super(cause);
    }

    public AddressException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
