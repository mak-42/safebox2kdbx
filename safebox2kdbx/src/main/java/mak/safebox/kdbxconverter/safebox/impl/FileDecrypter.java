package mak.safebox.kdbxconverter.safebox.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ContextedRuntimeException;

import lombok.RequiredArgsConstructor;

/**
 * Safebox file decrypter.
 */
@RequiredArgsConstructor
class FileDecrypter {

    /**
     * The header of Safebox encrypted files.
     */
    private static final String HEADER = "[com.zholdak.SafeBox Encrypted File]";

    /**
     * The folder keeping Safebox files.
     */
    private final Path filesPath;

    /**
     * Safebox data decrypter.
     */
    private final DataDecrypter decrypter;

    /**
     * Decrypts Safebox file.
     * 
     * @param fileUuid
     *            the Safebox file UUID
     * @return decrypted content of the Safebox file
     */
    public byte[] decrypt(final UUID fileUuid) {
        final Path filePath = filesPath.resolve(fileUuid.toString());
        try (InputStream inputStream = new DecodedContentInputStream(HEADER, Files.newInputStream(filePath), decrypter);
                ByteArrayOutputStream out = new ByteArrayOutputStream((int) Files.size(filePath))) {
            IOUtils.copy(inputStream, out);
            return out.toByteArray();
        } catch (final IOException exception) {
            throw new ContextedRuntimeException("Unable to read file", exception).setContextValue("fileUuid", fileUuid);
        }
    }
}
