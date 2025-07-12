package mak.safebox.kdbxconverter;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbDatabase;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mak.safebox.kdbxconverter.converter.Converter;
import mak.safebox.kdbxconverter.safebox.SafeboxDataProvider;
import mak.safebox.kdbxconverter.safebox.SafeboxDataProviderFactory;

/**
 * Main Application.
 */
@SpringBootApplication
@EnableConfigurationProperties(ConverterProperties.class)
@RequiredArgsConstructor
@Slf4j
public class Application implements CommandLineRunner {

    /**
     * Safebox data provider factory.
     */
    private final SafeboxDataProviderFactory dataProviderFactory;

    /**
     * Safebox converter properties.
     */
    private final ConverterProperties properties;

    /**
     * Safebox database to KDBX converter.
     */
    private final Converter converter;

    /**
     * Application main method.
     * 
     * @param args
     *            command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(final String... args) throws Exception {
        LOG.info("Loading target KDBX...");
        final KdbxCreds kdbxCreds = new KdbxCreds(properties.getTargetPassword().getBytes());
        final JaxbDatabase targetDb;
        if (Files.exists(properties.getTargetPath())) {
            try (InputStream is = Files.newInputStream(properties.getTargetPath())) {
                targetDb = JaxbDatabase.load(kdbxCreds, is);
            }
        } else {
            targetDb = JaxbDatabase.createEmptyDatabase();
            targetDb.setDescription("Converted from Safebox");
        }

        try (SafeboxDataProvider safeboxDataProvider =
                dataProviderFactory.create(properties.getSourcePath(), properties.getSourcePassword())) {
            converter.convert(safeboxDataProvider, targetDb);
        }

        LOG.info("Saving results...");
        try (OutputStream os = Files.newOutputStream(properties.getTargetPath(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            targetDb.save(kdbxCreds, os);
        }
        LOG.info("...done.");
    }
}
