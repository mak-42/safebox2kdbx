package mak.safebox.kdbxconverter.safebox;

import java.time.Instant;
import java.util.UUID;

/**
 * Safebox file attachment.
 *
 * @param id
 *            the unique identifier of the file
 * @param iconId
 *            the ID of the icon representing the file
 * @param title
 *            the title of the file
 * @param description
 *            the description of the file
 * @param params
 *            the parameters of the file
 * @param mimeType
 *            the MIME-type of the file
 * @param fileName
 *            the name of the file
 * @param fileSize
 *            the size of the file
 * @param fileTime
 *            the source time of the file
 * @param favorite
 *            flag indicating whether the file is marked as favorite
 * @param accessCounter
 *            the number of times the file was accessed
 * @param accessedAt
 *            the moment when the card file last accessed
 * @param createdAt
 *            the moment when the card file created
 * @param modifiedAt
 *            the moment when the card file last modified
 * @param content
 *            the content of the file
 */
public record SafeboxFile(UUID id,
    UUID iconId,
    String title,
    String description,
    String params,
    String mimeType,
    String fileName,
    long fileSize,
    Instant fileTime,
    boolean favorite,
    int accessCounter,
    Instant accessedAt,
    Instant createdAt,
    Instant modifiedAt,
    byte[] content) {
}
