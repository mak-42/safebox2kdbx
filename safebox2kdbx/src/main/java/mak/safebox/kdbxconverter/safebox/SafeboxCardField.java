package mak.safebox.kdbxconverter.safebox;

import java.time.Instant;
import java.util.UUID;

import org.springframework.lang.Nullable;

/**
 * Represents a field within a Safebox card.
 * 
 * @param id
 *            the unique identifier of the field
 * @param templateItemId
 *            the identifier of the template item associated with this field
 * @param title
 *            the title of the field
 * @param type
 *            the type of the field
 * @param value
 *            the value of the field
 * @param visible
 *            flag indicating whether the field's content is visible
 * @param modifiedAt
 *            the moment when the field was last modified
 */
public record SafeboxCardField(UUID id,
    UUID templateItemId,
    String title,
    SafeboxFiledType type,
    @Nullable
    Object value,
    boolean visible,
    Instant modifiedAt) {
}
