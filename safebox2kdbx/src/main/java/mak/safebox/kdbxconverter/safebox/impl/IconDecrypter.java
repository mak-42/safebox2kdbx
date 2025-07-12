/**
 * 
 */
package mak.safebox.kdbxconverter.safebox.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.lang3.exception.ContextedRuntimeException;

import lombok.RequiredArgsConstructor;

/**
 * Safebox icon decrypter.
 */
@RequiredArgsConstructor
class IconDecrypter {

    /**
     * The header of Safebox encrypted icons.
     */
    private static final String HEADER = "[com.zholdak.SafeBox Encrypted Icon]";

    /**
     * The folder keeping Safebox icons.
     */
    private final Path iconPath;

    /**
     * Safebox data decrypter.
     */
    private final DataDecrypter decrypter;

    /**
     * Decrypts Safebox icon.
     * 
     * @param iconFileName
     *            the Safebox icon file name
     * @return decrypted content of the Safebox icon
     */
    public byte[] decrypt(final UUID iconFileName) {
        final Path filePath = iconPath.resolve(iconFileName.toString());
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            final byte[] header = new byte[HEADER.length()];
            if ((inputStream.read(header) != header.length) || !Arrays.equals(header, HEADER.getBytes())) {
                throw new ContextedRuntimeException("Invalid icon file format")
                        .setContextValue("fileName", iconFileName);
            }

            final byte[] buffer = new byte[(int) (filePath.toFile().length() - HEADER.length())];
            final int read = inputStream.read(buffer);
            if (read != buffer.length) {
                throw new ContextedRuntimeException("Invalid file format")
                        .setContextValue("fileName", iconFileName)
                        .setContextValue("read", read)
                        .setContextValue("expected", buffer.length);
            }
            return decrypter.decrypt(buffer);
        } catch (final IOException exception) {
            throw new ContextedRuntimeException("Unable to read icon", exception)
                    .setContextValue("fileName", iconFileName);
        }

    }
}
