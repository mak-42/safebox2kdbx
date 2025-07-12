package mak.safebox.kdbxconverter.converter.impl;

import java.util.Map;
import java.util.UUID;

import org.linguafranca.pwdb.kdbx.jaxb.JaxbDatabase;

import mak.safebox.kdbxconverter.safebox.SafeboxDataProvider;
import mak.safebox.kdbxconverter.safebox.SafeboxTemplate;

/**
 * Context used during the conversion process.
 * 
 * @param sourceDataProvider
 *            the provider of data from the Safebox database
 * @param targetDb
 *            the target KDBX database where the converted data will be stored
 * @param id2IconInfo
 *            a map that associates icon IDs with their corresponding info of icon objects
 * @param id2Template
 *            a map that associates template IDs with their corresponding template objects
 */
record Context(SafeboxDataProvider sourceDataProvider,
    JaxbDatabase targetDb,
    Map<UUID, IconInfo> id2IconInfo,
    Map<UUID, SafeboxTemplate> id2Template) {
}
