package mak.safebox.kdbxconverter.converter;

import org.linguafranca.pwdb.kdbx.jaxb.JaxbDatabase;

import mak.safebox.kdbxconverter.safebox.SafeboxDataProvider;

/**
 * Safebox database to KDBX converter.
 */
public interface Converter {

    /**
     * Converts data from Safebox database into KDBX.
     * 
     * @param sourceDataProvider
     *            source Safebox data provider
     * @param targetDb
     *            target KDBX database
     */
    void convert(SafeboxDataProvider sourceDataProvider, JaxbDatabase targetDb);
}
