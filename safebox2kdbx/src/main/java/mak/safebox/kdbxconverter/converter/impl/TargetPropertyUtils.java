package mak.safebox.kdbxconverter.converter.impl;

import org.linguafranca.pwdb.kdbx.jaxb.binding.JaxbEntryBinding;
import org.linguafranca.pwdb.kdbx.jaxb.binding.ObjectFactory;
import org.linguafranca.pwdb.kdbx.jaxb.binding.StringField;
import org.springframework.lang.Nullable;

/**
 * Utility class for manipulating KDBX entry properties.
 */
final class TargetPropertyUtils {

    /**
     * Constructor.
     */
    private TargetPropertyUtils() {
        // nothing to do
    }

    /**
     * Adds a string property to a KDBX entry.
     * 
     * @param dg
     *            target entity element declaration
     * @param objectFactory
     *            target database object factory
     * @param name
     *            the name of the property
     * @param value
     *            the value of the property
     * @param protect
     *            flag indicating whether a content of the item should be protected or not
     */
    public static void addStringProperty(final JaxbEntryBinding dg,
        final ObjectFactory objectFactory,
        final String name,
        final @Nullable String value,
        final boolean protect) {
        final StringField.Value fieldValue = objectFactory.createStringFieldValue();
        fieldValue.setValue(value);
        fieldValue.protectOnOutput = protect;
        final StringField field = objectFactory.createStringField();
        field.setKey(name);
        field.setValue(fieldValue);
        dg.getString().add(field);
    }
}
