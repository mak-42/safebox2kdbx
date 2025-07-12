package mak.safebox.kdbxconverter.safebox;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Safebox card.
 * 
 * @param id
 *            the unique identifier of the card
 * @param folderId
 *            the ID of the parent folder of the card
 * @param templateId
 *            the ID of the template associated with the card
 * @param iconId
 *            the ID of the icon representing the card
 * @param title
 *            the title of the card
 * @param description
 *            the description of the card
 * @param favorite
 *            flag indicating whether the card is marked as favorite
 * @param fields
 *            the list of fields of the card
 * @param accessCounter
 *            the number of times the card was accessed
 * @param accessedAt
 *            the moment when the card was last accessed
 * @param createdAt
 *            the moment when the card was created
 * @param modifiedAt
 *            the moment when the card was last modified
 */
public record SafeboxCard(UUID id,
    UUID folderId,
    UUID templateId,
    UUID iconId,
    String title,
    String description,
    boolean favorite,
    int accessCounter,
    List<SafeboxCardField> fields,
    Instant accessedAt,
    Instant createdAt,
    Instant modifiedAt) {
}
