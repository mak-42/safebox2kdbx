package mak.safebox.kdbxconverter.safebox.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The encrypted file input stream.
 * <p/>
 * The stream reads the encrypted file, decrypts the content and returns it.
 */
@Slf4j
@RequiredArgsConstructor
class DecodedContentInputStream extends InputStream {

    /**
     * The state of the encrypted file input stream.
     * 
     * @see Stream
     */
    private enum State {
        /**
         * The initial state.
         * <p>
         * Nothing is read.
         */
        INITIAL,

        /**
         * The encrypted file is open for reading and can be read.
         */
        READ,

        /**
         * The end of the encrypted file has been reached.
         * <p>
         * Nothing to read.
         */
        EOF
    }

    /**
     * Number of bytes in an integer value.
     */
    private static final int BYTES_IN_INTEGER = Integer.SIZE / Byte.SIZE;

    /**
     * Unsigned byte mask.
     */
    private static final int UNSIGNED_BYTE_MASK = 0xFF;

    /**
     * The value which indicates that the stream reaches its end.
     */
    private static final int END_OF_STREAM_VALUE = -1;

    /**
     * The header of Safebox encrypted files.
     */
    private final String fileHeader;

    /**
     * The source encrypted file input stream.
     */
    private final InputStream inputStream;

    /**
     * Safebox data decrypter.
     */
    private final DataDecrypter decrypter;

    /**
     * State.
     */
    private State state = State.INITIAL;

    /**
     * The internal buffer.
     */
    private byte[] buffer = new byte[0];

    /**
     * The current cursor position in the internal buffer.
     */
    private int bufferPos = 0;

    /**
     * Number of unread bytes left.
     */
    private long bytesLeft;

    @Override
    public int read() throws IOException {
        int value = END_OF_STREAM_VALUE;
        boolean continueFlag;

        do {
            continueFlag = true;
            switch (state) {
                case INITIAL: {
                    initInputStream();

                    state = State.READ;
                    break;
                }
                case READ: {
                    if (bufferPos >= buffer.length) {
                        if (bytesLeft <= 0) {
                            state = State.EOF;
                        } else {
                            updateBuffer();
                        }
                    } else {
                        value = unsignedByte2Int(buffer[bufferPos++]);
                        continueFlag = false;
                    }
                    break;
                }
                default: {
                    value = END_OF_STREAM_VALUE;
                    continueFlag = false;
                    break;
                }
            }
        } while (continueFlag);
        return value;
    }

    /**
     * Updates the internal buffer with the decrypted content of the source encrypted file.
     * 
     * @throws IOException
     *             if an IO exception occurs
     */
    private void updateBuffer() throws IOException {
        final byte[] lengthBytes = new byte[BYTES_IN_INTEGER];
        if (inputStream.read(lengthBytes) != lengthBytes.length) {
            throw new IOException("Invalid file format");
        }
        LOG.debug(Arrays.toString(lengthBytes));
        final int length = bytesToInt(lengthBytes);
        LOG.debug("{}", length);
        final byte[] readBytes = new byte[length];
        if (inputStream.read(readBytes) != length) {
            throw new IOException("Invalid file format");
        }
        bytesLeft = bytesLeft - lengthBytes.length - length;
        buffer = decrypter.decrypt(readBytes);
        bufferPos = 0;
    }

    /**
     * Opens the source encrypted input file and check its header.
     * 
     * @throws FileNotFoundException
     *             if the source file is not found
     * @throws IOException
     *             if an IO exception occurs
     */
    private void initInputStream() throws FileNotFoundException, IOException {
        final int fileLength = inputStream.available();
        final int fileHeaderLength = fileHeader.length();
        final byte[] header = new byte[fileHeaderLength];
        if ((inputStream.read(header) != header.length) || !Arrays.equals(header, fileHeader.getBytes())) {
            throw new IOException("Invalid file format");
        }
        bytesLeft = fileLength - fileHeaderLength;
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        super.close();
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
