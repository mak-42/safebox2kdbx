package mak.safebox.kdbxconverter.converter.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.linguafranca.pwdb.Entry;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * The converter from Safebox field names to KDBX property names.
 */
@Service
@RequiredArgsConstructor
class FieldNameConverter {

    /**
     * Safebox fields name converter properties.
     */
    private final NameConverterProperties properties;

    /**
     * Converts Safebox field name to KDBX property name.
     * 
     * @param name
     *            source Safebox field name
     * @param allNames
     *            all source card field names
     * @return converted KDBX property name
     */
    public String convert(final String name, final List<String> allNames) {
        final String lcName = StringUtils.lowerCase(name);
        if (properties.getPassword().contains(lcName)) {
            for (int i = 0; i < allNames.size(); i++) {
                final String checkingName = allNames.get(i);
                if (checkingName.equals(name)) {
                    return Entry.STANDARD_PROPERTY_NAME_PASSWORD;
                }
                if (properties.getPassword().contains(StringUtils.lowerCase(checkingName))) {
                    break;
                }
            }
        } else if (properties.getUsername().contains(lcName)) {
            for (int i = 0; i < allNames.size(); i++) {
                final String checkingName = allNames.get(i);
                if (checkingName.equals(name)) {
                    return Entry.STANDARD_PROPERTY_NAME_USER_NAME;
                }
                if (properties.getUsername().contains(StringUtils.lowerCase(checkingName))) {
                    break;
                }
            }
        } else if (properties.getUrl().contains(lcName)) {
            for (int i = 0; i < allNames.size(); i++) {
                final String checkingName = allNames.get(i);
                if (checkingName.equals(name)) {
                    return Entry.STANDARD_PROPERTY_NAME_URL;
                }
                if (properties.getUrl().contains(StringUtils.lowerCase(checkingName))) {
                    break;
                }
            }
        }
        return name;
    }
}
