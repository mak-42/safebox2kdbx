/**
 * 
 */
package mak.safebox.kdbxconverter.safebox.impl;

import java.text.MessageFormat;
import java.util.Arrays;

/**
 * The class provides implementations for several numeric operations defined in Safebox.
 */
final class SafeboxNumbersHelper {

    /**
     * Number of bytes in an integer value.
     */
    public static final int BYTES_IN_INTEGER = Integer.SIZE / Byte.SIZE;

    /**
     * Unsigned byte mask.
     */
    private static final int UNSIGNED_BYTE_MASK = 0xFF;

    /**
     * Constructor.
     */
    private SafeboxNumbersHelper() {
        // nothing to do
    }

    /**
     * Converts unsigned byte value into integer value.
     * 
     * @param value
     *            the unsigned value to be converted
     * @return the result of the operation
     */
    public static int unsignedByte2Int(final byte value) {
        return UNSIGNED_BYTE_MASK & value;
    }

    /**
     * Converts an 4 bytes numeric value stored in the bytes array into an integer value.
     * 
     * @param values
     *            the array containing 4 bytes numeric value
     * @return an integer value stored in the source bytes array
     */
    public static int bytesToInt(final byte[] values) {
        if ((values == null) || (values.length < BYTES_IN_INTEGER)) {
            throw new IllegalArgumentException(
                    MessageFormat.format("Unable to process ''{0}''", Arrays.toString(values)));
        }

        int result = 0;
        int shift = 0;
        for (int i = 0; i < BYTES_IN_INTEGER; i++) {
            result |= unsignedByte2Int(values[i]) << shift;
            shift += Byte.SIZE;
        }
        return result;
    }

}
