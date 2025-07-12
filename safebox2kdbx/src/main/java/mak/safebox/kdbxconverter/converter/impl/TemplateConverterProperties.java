package mak.safebox.kdbxconverter.converter.impl;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Safebox card template converter properties.
 */
@Data
@Validated
@ConfigurationProperties("converter.template")
class TemplateConverterProperties {

    /**
     * The default number of lines allocated for text fields.
     */
    private static final int DEFAULT_NUMBER_OF_LINES_FOR_TEXT = 15;

    /**
     * KDBX templates group.
     */
    @NotNull
    private String targetTemplatesGroup = "Templates";

    /**
     * The number of lines allocated for text fields.
     */
    @Min(1)
    private int textLinesNumber = DEFAULT_NUMBER_OF_LINES_FOR_TEXT;
}
