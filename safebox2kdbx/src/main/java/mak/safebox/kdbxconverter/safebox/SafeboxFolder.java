package mak.safebox.kdbxconverter.safebox;

import java.time.Instant;
import java.util.UUID;

import org.springframework.lang.Nullable;

/**
 * Safebox folder.
 * 
 * @param id
 *            ID
 * @param parentId
 *            parent ID if exists
 * @param iconId
 *            icon ID
 * @param templateId
 *            template ID
 * @param title
 *            title
 * @param description
 *            description
 * @param params
 *            the parameters
 * @param createdAt
 *            the he moment the record was created
 * @param modifiedAt
 *            the he moment the record was last modified
 */
public record SafeboxFolder(UUID id,
    @Nullable
    UUID parentId,
    UUID iconId,
    @Nullable
    UUID templateId,
    String title,
    String description,
    @Nullable
    String params,
    Instant createdAt,
    Instant modifiedAt) {
}
