package com.alteredworlds.taptap.util;

/**
 * Created by twcgilbert on 22/12/2015.
 */
public class Primitives {
    /**
     * Convert little endian bytes representing int32_t into int
     */
    public static int int32_tToInt(byte b0, byte b1, byte b2, byte b3) {
        return unsignedByteToInt(b0) +
                (unsignedByteToInt(b1) << 8) +
                (unsignedByteToInt(b2) << 16) +
                (unsignedByteToInt(b3) << 24);
    }

    /**
     * Convert little endian bytes representing uint32_t into long
     * NOTE: can't use int, not big enough since is SIGNED 32 bit
     */
    public static long uint32_tToLong(byte b0, byte b1, byte b2, byte b3) {
        return (unsignedByteToInt(b0) +
                (unsignedByteToInt(b1) << 8) +
                (unsignedByteToInt(b2) << 16) +
                (unsignedByteToInt(b3) << 24)) &
                0xFFFFFFFFL;
    }

    /**
     * Convert signed byte to int
     *
     * i.e. Java has no unsigned byte type, so assuming param
     * actually represents unsigned byte value, put this value into a signed type
     * large enough to hold the information, ignoring spurious 'sign'
     * This returns the value as a (signed) int.
     */
    public static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    /**
     * Convert 2 unsigned bytes representing an int16_t into int
     */
    public static int int16_tToInt(byte b0, byte b1) {
        // Java (signed) int is 32 bits
        // put value into MS 16 bits to properly interpret sign bit
        // then (signed) right shift 16 bits to get correct magnitude.
        return ((unsignedByteToInt(b0) << 16) + (unsignedByteToInt(b1) << 24)) >> 16;
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
