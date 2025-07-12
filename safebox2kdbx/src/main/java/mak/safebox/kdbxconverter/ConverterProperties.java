package mak.safebox.kdbxconverter;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Safebox converter properties.
 */
@Data
@Validated
@ConfigurationProperties("converter")
public class ConverterProperties {

    /**
     * Source Safebox data path.
     */
    @NotNull
    private Path sourcePath;

    /**
     * Source Safebox password.
     */
    @NotNull
    private String sourcePassword;

    /**
     * Target KDBX data path.
     */
    @NotNull
    private Path targetPath = Paths.get("./target");

    /**
     * Target KDBX password.
     */
    @NotNull
    private String targetPassword = "password";

    /**
     * The path of log file.
     */
    @Nullable
    private Path logFile;
}
