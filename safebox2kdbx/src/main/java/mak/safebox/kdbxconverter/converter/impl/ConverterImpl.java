package mak.safebox.kdbxconverter.converter.impl;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbDatabase;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbGroup;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbIcon;
import org.linguafranca.pwdb.kdbx.jaxb.binding.KeePassFile.Meta;
import org.linguafranca.pwdb.kdbx.jaxb.binding.ObjectFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mak.safebox.kdbxconverter.converter.Converter;
import mak.safebox.kdbxconverter.safebox.SafeboxDataProvider;
import mak.safebox.kdbxconverter.safebox.SafeboxTemplate;

/**
 * Implementation of Safabox database to KDBX converter.
 */
@Service
@Slf4j
@RequiredArgsConstructor
class ConverterImpl implements Converter {

    /**
     * Converter for Safabox database icons to KDBX icons.
     */
    private final IconConverter iconConverter;

    /**
     * Converter for Safabox database card templates to KDBX card templates compatible with
     * KPEntryTemplatesplugin/KeePassDx.
     */
    private final TemplateConverter templateConverter;

    /**
     * Converter for Safabox database folders to KDBX groups.
     */
    private final FolderConverter folderConverter;

    /**
     * Safebox orphaned folders converter properties.
     */
    private final OrphanedConverterProperties orphanedConverterProperties;

    @Override
    public void convert(final SafeboxDataProvider sourceDataProvider, final JaxbDatabase targetDb) {
        LOG.info("Converting...");
        setupDb(targetDb);

        LOG.info("Loading icons...");
        final Map<UUID, IconInfo> id2IconInfo = iconConverter.createId2IconInfo(sourceDataProvider);

        LOG.info("Creating context...");
        final Map<UUID, SafeboxTemplate> id2Template = templateConverter.createId2Template(sourceDataProvider);
        final Context context = new Context(sourceDataProvider, targetDb, id2IconInfo, id2Template);

        LOG.info("Converting templates...");
        id2Template.keySet().forEach(templateId -> templateConverter.processTemplate(templateId, context));

        LOG.info("Converting folders...");
        context.sourceDataProvider().folders(null)
                .forEachOrdered(child -> folderConverter.processFolder(child, targetDb.getRootGroup(), EMPTY, context));

        LOG.info("Converting orphaned folders...");
        orphanedConverterProperties.getOrphanedGroupName();
        final String orphanedGroupName = orphanedConverterProperties.getOrphanedGroupName();
        final JaxbGroup targetOrphanedGroup = targetDb.getRootGroup().getGroups().stream()
                .filter(grp -> orphanedGroupName.equals(grp.getName())).findFirst().orElseGet(() -> {
                    final JaxbGroup newGroup = context.targetDb().newGroup(orphanedGroupName);
                    newGroup.setIcon(new JaxbIcon(orphanedConverterProperties.getOrphanedGroupIcon()));
                    targetDb.getRootGroup().addGroup(newGroup);
                    return newGroup;
                });
        context.sourceDataProvider().orphanedFolders()
                .forEachOrdered(child -> folderConverter.processFolder(child, targetOrphanedGroup, EMPTY, context));
    }

    /**
     * Setting up parameters of target KDBX database.
     * 
     * @param targetDb
     *            target KDBX database
     */
    private void setupDb(final JaxbDatabase targetDb) {
        final ObjectFactory objectFactory = getObjectFactory(targetDb);
        final Meta meta = targetDb.getKeePassFile().getMeta();
        if (meta.getBinaries() == null) {
            meta.setBinaries(objectFactory.createBinaries());
        }
    }

    /**
     * Gets target KDBX objects factory.
     * 
     * @param tergetDb
     *            the target KDBX database
     * @return target KDBX objects factory
     */
    private ObjectFactory getObjectFactory(final JaxbDatabase tergetDb) {
        try {
            return (ObjectFactory) FieldUtils.readDeclaredField(tergetDb, "objectFactory", true);
        } catch (final IllegalAccessException exception) {
            throw new ContextedRuntimeException("Unable to get objectFactory", exception);
        }
    }
}
