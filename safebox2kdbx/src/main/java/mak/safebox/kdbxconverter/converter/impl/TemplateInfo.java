package mak.safebox.kdbxconverter.converter.impl;

import lombok.Data;
import mak.safebox.kdbxconverter.safebox.SafeboxTemplate;

/**
 * Source Template info.
 */
@Data
class TemplateInfo {

    /**
     * The Safebox template.
     */
    private final SafeboxTemplate template;

    /**
     * The flag indicates if the template has been converted.
     */
    private boolean converted = false;
}
