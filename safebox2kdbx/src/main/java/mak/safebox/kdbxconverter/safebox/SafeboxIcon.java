package mak.safebox.kdbxconverter.safebox;

import java.time.Instant;
import java.util.UUID;

/**
 * Safebox icon.
 * 
 * @param id
 *            ID
 * @param fileName
 *            the icon file name
 * @param hash
 *            hash of content
 * @param createdAt
 *            the he moment the record was created
 * @param modifiedAt
 *            the he moment the record was last modified
 * @param content
 *            the content of icon
 */
public record SafeboxIcon(UUID id, UUID fileName, String hash, Instant createdAt, Instant modifiedAt, byte[] content) {
}
