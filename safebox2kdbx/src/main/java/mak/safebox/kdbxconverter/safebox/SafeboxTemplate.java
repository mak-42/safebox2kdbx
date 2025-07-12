/**
 * 
 */
package mak.safebox.kdbxconverter.safebox;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Represents a safebox template.
 * 
 * @param id
 *            the unique identifier of the template
 * @param iconId
 *            the identifier of the associated icon
 * @param title
 *            the title of the template
 * @param description
 *            the description of the template
 * @param items
 *            the list of field descriptions that make up the template
 * @param createdAt
 *            the moment the template was created
 * @param modifiedAt
 *            the moment the template was last modified
 */
public record SafeboxTemplate(UUID id,
    UUID iconId,
    String title,
    String description,
    List<SafeboxTemplateItem> items,
    Instant createdAt,
    Instant modifiedAt) {
}
