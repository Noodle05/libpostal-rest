package org.gaofamily.libpostal.model.codec;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.UUID;

/**
 * @author Wei Gao
 * @since 8/16/16
 */
public abstract class CodecHelper {
    private static final Charset utf8 = Charset.forName("UTF-8");
    public static final byte[] delimiter = new byte[]{(byte) 0xf0, (byte) 0xf0, (byte) 0xf0, (byte) 0xf0};

    public static String readString(ByteBuf byteBuf) {
        int len = byteBuf.readInt();
        if (len < 0) {
            return null;
        } else if (len == 0) {
            return "";
        } else {
            byte[] bytes = new byte[len];
            byteBuf.readBytes(bytes);
            return new String(bytes, utf8);
        }
    }

    public static void writeString(ByteBuf byteBuf, String val) {
        if (val == null) {
            byteBuf.writeInt(-1);
        } else {
            byte[] bytes = val.getBytes(utf8);
            byteBuf.writeInt(bytes.length);
            byteBuf.writeBytes(bytes);
        }
    }

    public static UUID readUUID(ByteBuf byteBuf) {
        long most = byteBuf.readLong();
        long least = byteBuf.readLong();
        UUID uuid = new UUID(most, least);
        return uuid;
    }

    public static void writeUUID(ByteBuf byteBuf, UUID val) {
        byteBuf.writeLong(val.getMostSignificantBits());
        byteBuf.writeLong(val.getLeastSignificantBits());
    }
}
