package com.alteredworlds.taptap.util;

/**
 * Created by twcgilbert on 22/12/2015.
 */
public class Primitives {
    /**
     * Convert signed bytes to a 32-bit unsigned int.
     */
    public static int unsignedBytesToInt(byte b0, byte b1, byte b2, byte b3) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8))
                + (unsignedByteToInt(b2) << 16) + (unsignedByteToInt(b3) << 24);
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    public static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    public static int unsignedBytesToInt(byte b0, byte b1) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
    }

    /**
     * Convert unsigned long to len signed bytes
     */
    public static void unsignedIntToBytes(byte[] buf, int offset, int len, int value) {
        for (int i = len - 1; i >= 0; i--) {
            buf[i + offset] = (byte) (value & 0xFF);
            value >>= 8;
        }
    }
}
