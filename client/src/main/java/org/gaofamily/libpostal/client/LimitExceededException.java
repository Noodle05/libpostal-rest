package org.gaofamily.libpostal.client;

/**
 * @author Wei Gao
 * @since 8/19/16
 */
public class LimitExceededException extends AddressException {
    private final int limit;
    private final int actual;

    public LimitExceededException(int limit, int actual) {
        super();
        this.limit = limit;
        this.actual = actual;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("Request size exceed limit.");
        sb.append(" Limit: ").append(limit).append(", actual size: ").append(actual);
        return sb.toString();
    }
}
