package mak.safebox.kdbxconverter.safebox;

import java.time.Instant;
import java.util.UUID;

/**
 * Safebox template item.
 * 
 * @param id
 *            the unique identifier of the item
 * @param title
 *            the title of the item
 * @param type
 *            the type of the item
 * @param visible
 *            flag indicating whether a content of the item is visible or not
 * @param createdAt
 *            the moment when the item was created
 * @param modifiedAt
 *            the moment when the item was last modified
 */
public record SafeboxTemplateItem(UUID id,
    String title,
    SafeboxFiledType type,
    boolean visible,
    Instant createdAt,
    Instant modifiedAt) {
}
