package mak.safebox.kdbxconverter.safebox;

import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

/**
 * Safebox data provider.
 */
public interface SafeboxDataProvider extends AutoCloseable {

    /**
     * Starts the stream of Safebox templates.
     * 
     * @return the stream of Safebox templates
     */
    Stream<SafeboxTemplate> templates();

    /**
     * Starts the stream of Safebox icons.
     * 
     * @return the stream of Safebox icons
     */
    Stream<SafeboxIcon> icons();

    /**
     * Starts the stream of Safebox folders.
     * 
     * @param parentId
     *            ID of parent folder (<code>null</code> for root folder)
     * @return the stream of Safebox folders
     */
    Stream<SafeboxFolder> folders(@Nullable
    UUID parentId);

    /**
     * Starts the stream of Safebox orphaned folders.
     * 
     * @return the stream of Safebox orphaned folders
     */
    Stream<SafeboxFolder> orphanedFolders();

    /**
     * Starts the stream of Safebox cards.
     * 
     * @param folderId
     *            parent folder ID of the card
     * @return the stream of Safebox cards
     */
    Stream<SafeboxCard> cards(UUID folderId);

    /**
     * Starts the stream of Safebox files.
     * 
     * @param containerId
     *            the contained ID of the file
     * @return the stream of Safebox files
     */
    Stream<SafeboxFile> files(UUID containerId);
}
