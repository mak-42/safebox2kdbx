package mak.safebox.kdbxconverter.converter.impl;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Safebox orphaned folders converter properties.
 */
@Data
@Validated
@ConfigurationProperties("converter.orphaned")
class OrphanedConverterProperties {

    /**
     * Tools icon code.
     */
    private static final int TOOLS_ICON = 59;

    /**
     * Orphaned group name.
     */
    @NotNull
    private String orphanedGroupName = "Orphaned";

    /**
     * Orphaned group icon number.
     */
    @Min(0)
    private int orphanedGroupIcon = TOOLS_ICON;
}
