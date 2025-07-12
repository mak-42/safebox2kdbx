package mak.safebox.kdbxconverter.converter.impl;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.Optional.ofNullable;
import static mak.safebox.kdbxconverter.converter.impl.TargetPropertyUtils.addStringProperty;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbDatabase;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbEntry;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbGroup;
import org.linguafranca.pwdb.kdbx.jaxb.binding.CustomData;
import org.linguafranca.pwdb.kdbx.jaxb.binding.JaxbEntryBinding;
import org.linguafranca.pwdb.kdbx.jaxb.binding.JaxbGroupBinding;
import org.linguafranca.pwdb.kdbx.jaxb.binding.ObjectFactory;
import org.linguafranca.pwdb.kdbx.jaxb.binding.Times;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mak.safebox.kdbxconverter.safebox.SafeboxCardField;
import mak.safebox.kdbxconverter.safebox.SafeboxFolder;

/**
 * Converter for Safabox database folders to KDBX groups.
 */
@Service
@Slf4j
@RequiredArgsConstructor
class FolderConverter {

    /**
     * Converter for Safabox database icons to KDBX icons.
     */
    private final IconConverter iconConverter;

    /**
     * The converter from Safebox field names to KDBX property names.
     */
    private final FieldNameConverter fieldNameConverter;

    /**
     * Converts a folder from Safebox database to a KDBX group.
     * 
     * @param sourceFolder
     *            the source folder in the Safebox database
     * @param targetParent
     *            the target parent group in KDBX where the converted folder will be created
     * @param path
     *            the path of the folder
     * @param context
     *            the context used during the conversion process
     */
    public void processFolder(final SafeboxFolder sourceFolder,
        final JaxbGroup targetParent,
        final String path,
        final Context context) {
        LOG.debug("Converting folder: {}", sourceFolder);
        final UUID sourceFolderId = sourceFolder.id();
        final String folderTitle = sourceFolder.title();
        final String folderPath = path + "/" + folderTitle;

        if (targetParent.getGroups().stream().anyMatch(grp -> sourceFolderId.equals(grp.getUuid()))) {
            LOG.info("Folder was converted before and been skipped: id={}, path={}", sourceFolderId, folderPath);
            return;
        }

        final JaxbGroup targetGroup = context.targetDb().newGroup(folderTitle);
        targetParent.addGroup(targetGroup);
        try {
            final JaxbGroupBinding dg = (JaxbGroupBinding) FieldUtils.readDeclaredField(targetGroup, "delegate", true);
            dg.setUUID(sourceFolderId);
            ofNullable(sourceFolder.description()).ifPresent(dg::setNotes);

            final UUID iconId = sourceFolder.iconId();
            iconConverter.processIconIfRequired(iconId, context);
            dg.setCustomIconUUID(iconId);

            final CustomData.Item oldIdItem = new CustomData.Item();
            oldIdItem.setKey("originalSafeboxId");
            oldIdItem.setValue(sourceFolder.id().toString());

            final ObjectFactory objectFactory = new ObjectFactory();
            CustomData customData = dg.getCustomData();
            if (customData == null) {
                customData = objectFactory.createCustomData();
                dg.setCustomData(customData);
            }
            customData.getItem().add(oldIdItem);
        } catch (final IllegalAccessException exception) {
            throw new ContextedRuntimeException("Unable to set properties of folder", exception)
                    .addContextValue("sourceFolder", sourceFolder);
        }

        convertCards(sourceFolderId, targetGroup, folderPath, context);
        convertFiles(sourceFolderId, targetGroup, folderPath, context);

        context.sourceDataProvider().folders(sourceFolder.id())
                .forEachOrdered(child -> processFolder(child, targetGroup, folderPath, context));
        LOG.info("Folder has been converted: id={}, path={}", sourceFolderId, folderPath);
    }

    /**
     * Converts cards within to the source folder from Safebox database to a KDBX group.
     * 
     * @param sourceFolderId
     *            ID of the source folder in the Safebox database
     * @param targetGroup
     *            the target group in KDBX
     * @param path
     *            the path of the folder
     * @param context
     *            the context used during the conversion process
     */
    private void convertCards(final UUID sourceFolderId,
        final JaxbGroup targetGroup,
        final String path,
        final Context context) {
        context.sourceDataProvider().cards(sourceFolderId).forEachOrdered(sourceCard -> {
            final UUID sourceCardId = sourceCard.id();
            final String cardTitle = sourceCard.title();
            final JaxbEntry targetCard = context.targetDb().newEntry(cardTitle);
            ofNullable(sourceCard.description()).ifPresent(targetCard::setNotes);
            try {
                final JaxbEntryBinding dg =
                        (JaxbEntryBinding) FieldUtils.readDeclaredField(targetCard, "delegate", true);
                dg.setUUID(sourceCardId);

                final UUID iconId = sourceCard.iconId();
                iconConverter.processIconIfRequired(iconId, context);
                dg.setCustomIconUUID(iconId);
                final Times times = dg.getTimes();
                times.setCreationTime(Date.from(sourceCard.createdAt()));
                times.setLastAccessTime(Date.from(sourceCard.accessedAt()));
                times.setLastModificationTime(Date.from(sourceCard.modifiedAt()));
                times.setUsageCount(sourceCard.accessCounter());

                if (sourceCard.favorite()) {
                    dg.setTags("favorites");
                }

                final ObjectFactory objectFactory = getObjectFactory(context.targetDb());
                ofNullable(context.id2Template().get(sourceCard.templateId()))
                        .ifPresent(template -> addStringProperty(dg,
                                objectFactory,
                                "_etm_template_uuid",
                                convertTemplateIdToString(template.id()),
                                false));

                final List<SafeboxCardField> fields = sourceCard.fields();
                final List<String> fieldNames = fields.stream().map(SafeboxCardField::title).toList();
                for (int i = 0; i < fields.size(); i++) {
                    final SafeboxCardField sourceField = fields.get(i);
                    final String title = fieldNameConverter.convert(sourceField.title(), fieldNames);
                    final Object sourceValue = sourceField.value();
                    final boolean visible = sourceField.visible();
                    switch (sourceField.type()) {
                        case SFT_DATE:
                        case SFT_MONTH_YEAR: {
                            addStringProperty(dg,
                                    objectFactory,
                                    title,
                                    sourceValue == null ? null : ISO_INSTANT.format((Instant) sourceValue),
                                    !visible);
                            break;
                        }
                        case SFT_NUMBER:
                        case SFT_PHONE:
                        case SFT_STRING:
                        case SFT_TEXT:
                        case SFT_URI: {
                            addStringProperty(dg,
                                    objectFactory,
                                    title,
                                    sourceValue == null ? null : String.valueOf(sourceValue),
                                    !visible);
                            break;
                        }
                    }

                    final CustomData.Item oldIdItem = new CustomData.Item();
                    oldIdItem.setKey("originalSafeboxItemId_" + title);
                    oldIdItem.setValue(sourceField.id().toString());

                    CustomData customData = dg.getCustomData();
                    if (customData == null) {
                        customData = objectFactory.createCustomData();
                        dg.setCustomData(customData);
                    }
                    customData.getItem().add(oldIdItem);
                }

                context.sourceDataProvider().files(sourceCardId).forEachOrdered(sourceFile -> {
                    targetCard.setBinaryProperty(sourceFile.fileName(), sourceFile.content());
                    LOG.info("File has been converted: id={}, path={}/{}/{}",
                            sourceCardId,
                            path,
                            cardTitle,
                            sourceFile.title());
                });
            } catch (final IllegalAccessException exception) {
                throw new ContextedRuntimeException("Unable to set properties of card", exception)
                        .addContextValue("sourceCard", sourceCard);
            }

            targetGroup.addEntry(targetCard);
            LOG.info("Card has been converted: id={}, path={}/{}", sourceCardId, path, cardTitle);
        });
    }

    /**
     * Converts files attached to the source folder from Safebox database to a KDBX group.
     * 
     * @param sourceFolderId
     *            ID of the source folder in the Safebox database
     * @param targetGroup
     *            the target group in KDBX
     * @param path
     *            the path of the folder
     * @param context
     *            the context used during the conversion process
     */
    private void convertFiles(final UUID sourceFolderId,
        final JaxbGroup targetGroup,
        final String path,
        final Context context) {
        context.sourceDataProvider().files(sourceFolderId).forEachOrdered(sourceFile -> {
            final UUID sourceCardId = sourceFile.id();
            final JaxbEntry targetCard = context.targetDb().newEntry(sourceFile.title());
            ofNullable(sourceFile.description()).ifPresent(targetCard::setNotes);
            try {
                final JaxbEntryBinding dg =
                        (JaxbEntryBinding) FieldUtils.readDeclaredField(targetCard, "delegate", true);
                dg.setUUID(sourceCardId);

                final UUID iconId = sourceFile.iconId();
                iconConverter.processIconIfRequired(iconId, context);
                dg.setCustomIconUUID(iconId);
                final Times times = dg.getTimes();
                times.setCreationTime(Date.from(sourceFile.createdAt()));
                times.setLastAccessTime(Date.from(sourceFile.accessedAt()));
                times.setLastModificationTime(Date.from(sourceFile.modifiedAt()));
                times.setUsageCount(sourceFile.accessCounter());

                if (sourceFile.favorite()) {
                    dg.setTags("favorites");
                }

                final ObjectFactory objectFactory = getObjectFactory(context.targetDb());
                addStringProperty(dg, objectFactory, "fileSize", String.valueOf(sourceFile.fileSize()), false);
                addStringProperty(dg, objectFactory, "fileMineType", sourceFile.mimeType(), false);
                addStringProperty(dg, objectFactory, "fileTime", ISO_INSTANT.format(sourceFile.fileTime()), false);
                targetCard.setBinaryProperty(sourceFile.fileName(), sourceFile.content());
            } catch (final IllegalAccessException exception) {
                throw new ContextedRuntimeException("Unable to set properties of file", exception)
                        .addContextValue("sourceFile", sourceFile);
            }

            targetGroup.addEntry(targetCard);
            LOG.info("File has been converted: id={}, path={}/{}", sourceCardId, path, sourceFile.title());
        });
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

    /**
     * Converts a KDBX template ID to a string representation.
     * 
     * @param value
     *            KDBX template ID
     * @return string representation of the KDBX template ID
     */
    private String convertTemplateIdToString(final UUID value) {
        return value.toString().replace("-", "").toUpperCase();
    }
}
