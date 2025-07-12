package mak.safebox.kdbxconverter.safebox;

import java.nio.file.Path;

/**
 * Safebox data provider factory.
 */
public interface SafeboxDataProviderFactory {

    /**
     * Returns newly created data provider.
     * 
     * @param path
     *            Safebox database path
     * @param password
     *            Safebox database password
     * @return Safebox data provider
     */
    SafeboxDataProvider create(Path path, String password);
}
