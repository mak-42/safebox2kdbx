package mak.safebox.kdbxconverter.converter.impl;

import java.time.Instant;

import org.springframework.core.io.Resource;

import lombok.Data;

/**
 * Source icon info.
 */
@Data
class IconInfo {

    /**
     * The name of the icon.
     */
    private final String name;

    /**
     * The binary content of the icon.
     */
    private final Resource content;

    /**
     * The moment the record was last modified.
     */
    private final Instant modifiedAt;

    /**
     * The flag indicates if the icon has been converted.
     */
    private boolean converted = false;
}
