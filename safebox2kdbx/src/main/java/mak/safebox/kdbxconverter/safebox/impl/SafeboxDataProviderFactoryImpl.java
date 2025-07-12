package mak.safebox.kdbxconverter.safebox.impl;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.springframework.stereotype.Service;

import mak.safebox.kdbxconverter.safebox.SafeboxDataProvider;
import mak.safebox.kdbxconverter.safebox.SafeboxDataProviderFactory;

/**
 * The Safebox data provider factory implementation.
 */
@Service
class SafeboxDataProviderFactoryImpl implements SafeboxDataProviderFactory {

    @Override
    public SafeboxDataProvider create(final Path path, final String password) {
        final DataDecrypter decrypter = new DataDecrypter(password);
        final IconDecrypter iconDecrypter = new IconDecrypter(path.resolve("Icons"), decrypter);
        final FileDecrypter fileDecrypter = new FileDecrypter(path.resolve("Files"), decrypter);
        return new SafeboxDataProviderImpl(createDBConnection(path), decrypter, iconDecrypter, fileDecrypter);
    }

    /**
     * Creates connection to Safebox database.
     * 
     * @param path
     *            Safebox database folder path
     * @return the opened connection to Safebox database
     */
    private Connection createDBConnection(final Path path) {
        try {
            Class.forName("org.sqlite.JDBC");
            final Connection connection =
                    DriverManager.getConnection("jdbc:sqlite:" + path.resolve("safeboxpro.db") + "?open_mode=1");
            return connection;
        } catch (final Exception exception) {
            throw new ContextedRuntimeException("Unable to connect to Safebox database", exception)
                    .setContextValue("path", path);
        }
    }
}
