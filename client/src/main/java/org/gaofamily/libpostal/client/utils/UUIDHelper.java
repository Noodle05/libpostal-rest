package org.gaofamily.libpostal.client.utils;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @author Wei Gao
 * @since 2/11/17
 */
public abstract class UUIDHelper {
    public static byte[] toBytes(UUID uuid) {
        if (uuid == null) {
            throw new NullPointerException();
        }
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static UUID fromBytes(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException();
        }
        if (bytes.length != 16) {
            throw new IllegalArgumentException("Bytes length is " + bytes.length + ", need exact 16 bytes to work.");
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }
}
