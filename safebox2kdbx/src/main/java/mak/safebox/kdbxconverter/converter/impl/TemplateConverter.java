package mak.safebox.kdbxconverter.converter.impl;

import static java.util.Optional.ofNullable;
import static mak.safebox.kdbxconverter.converter.impl.TargetPropertyUtils.addStringProperty;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbDatabase;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbEntry;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbGroup;
import org.linguafranca.pwdb.kdbx.jaxb.binding.CustomData;
import org.linguafranca.pwdb.kdbx.jaxb.binding.JaxbEntryBinding;
import org.linguafranca.pwdb.kdbx.jaxb.binding.KeePassFile.Meta;
import org.linguafranca.pwdb.kdbx.jaxb.binding.ObjectFactory;
import org.linguafranca.pwdb.kdbx.jaxb.binding.Times;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mak.safebox.kdbxconverter.safebox.SafeboxDataProvider;
import mak.safebox.kdbxconverter.safebox.SafeboxFiledType;
import mak.safebox.kdbxconverter.safebox.SafeboxTemplate;
import mak.safebox.kdbxconverter.safebox.SafeboxTemplateItem;

/**
 * Converter for Safabox database card templates to KDBX card templates compatible with KPEntryTemplates
 * plugin/KeePassDx.
 */
@Service
@Slf4j
@RequiredArgsConstructor
class TemplateConverter {

    /**
     * NULL UUID.
     */
    private static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    /**
     * Converter for Safabox database icons to KDBX icons.
     */
    private final IconConverter iconConverter;

    /**
     * The converter from Safebox field names to KDBX property names.
     */
    private final FieldNameConverter fieldNameConverter;

    /**
     * Safebox card template converter properties.
     */
    private final TemplateConverterProperties properties;

    /**
     * Creates a map that associates Safebox card template IDs with their corresponding template objects.
     * 
     * @param sourceDataProvider
     *            Safebox data provider
     * @return created map that associates Safebox card template IDs with their corresponding template objects
     */
    public Map<UUID, SafeboxTemplate> createId2Template(final SafeboxDataProvider sourceDataProvider) {
        return sourceDataProvider.templates().collect(Collectors.toMap(SafeboxTemplate::id, Function.identity()));
    }

    /**
     * Converts the template if it has not been already converted.
     * 
     * @param templateId
     *            the ID of the template
     * @param context
     *            the context used during the conversion process
     */
    public void processTemplate(final UUID templateId, final Context context) {
        final SafeboxTemplate sourceTemplate = context.id2Template().get(templateId);
        if (sourceTemplate == null) {
            // The card has it's own template.
            return;
        }

        final String templatesGroupName = properties.getTargetTemplatesGroup();
        final JaxbDatabase targetDb = context.targetDb();
        final JaxbGroup rootGroup = targetDb.getRootGroup();
        final JaxbGroup templatesGroup = rootGroup.getGroups().stream()
                .filter(group -> templatesGroupName.equals(group.getName())).findFirst().orElseGet(() -> {
                    final JaxbGroup targetGroup = context.targetDb().newGroup(templatesGroupName);
                    rootGroup.addGroup(targetGroup);
                    final UUID newGroupUuid = targetGroup.getUuid();
                    final Meta meta = targetDb.getKeePassFile().getMeta();
                    meta.setEntryTemplatesGroup(newGroupUuid);
                    meta.setEntryTemplatesGroupChanged(new Date());
                    LOG.info("New templates group has been created: id={}, title={}", newGroupUuid, templatesGroupName);
                    return targetGroup;
                });

        final UUID sourceTemplateId = sourceTemplate.id();
        final String title = sourceTemplate.title();
        if (templatesGroup.getEntries().stream().anyMatch(grp -> sourceTemplateId.equals(grp.getUuid()))) {
            LOG.info("Template was converted before and been skipped: id={}, title={}", sourceTemplateId, title);
            return;
        }
        final JaxbEntry targetTemplate = context.targetDb().newEntry(title);
        templatesGroup.addEntry(targetTemplate);
        ofNullable(sourceTemplate.description()).ifPresent(targetTemplate::setNotes);
        try {
            final JaxbEntryBinding dg =
                    (JaxbEntryBinding) FieldUtils.readDeclaredField(targetTemplate, "delegate", true);
            dg.setUUID(sourceTemplateId);

            final UUID iconId = sourceTemplate.iconId();
            iconConverter.processIconIfRequired(iconId, context);
            dg.setCustomIconUUID(iconId);
            final Times times = dg.getTimes();
            times.setCreationTime(Date.from(sourceTemplate.createdAt()));
            times.setLastModificationTime(Date.from(sourceTemplate.modifiedAt()));

            final ObjectFactory objectFactory =
                    (ObjectFactory) FieldUtils.readDeclaredField(context.targetDb(), "objectFactory", true);
            addStringProperty(dg, objectFactory, "_etm_template", String.valueOf(1), false);

            final List<SafeboxTemplateItem> sourceItems = sourceTemplate.items();
            final List<String> fieldNames = sourceItems.stream().map(SafeboxTemplateItem::title).toList();

            // Add title to the template.
            convertItem(dg,
                    objectFactory,
                    new SafeboxTemplateItem(NULL_UUID,
                            "Title",
                            SafeboxFiledType.SFT_STRING,
                            true,
                            Instant.now(),
                            Instant.now()),
                    0,
                    fieldNames);
            for (int i = 0; i < sourceItems.size(); i++) {
                convertItem(dg, objectFactory, sourceItems.get(i), i + 1, fieldNames);
            }

        } catch (final IllegalAccessException exception) {
            throw new ContextedRuntimeException("Unable to set properties of template", exception)
                    .addContextValue("sourceTemplate", sourceTemplate);
        }
        LOG.info("Template has been converted: id={}, title={}", sourceTemplateId, title);
    }

    /**
     * Converts Safebox template item into KDBX entity field.
     * 
     * @param dg
     *            target entity element declaration
     * @param objectFactory
     *            target database object factory
     * @param sourceItem
     *            the source Safebox template item to convert
     * @param position
     *            the index position of the item in the source list, used for ordering
     * @param fieldNames
     *            all template field names
     */
    private void convertItem(final JaxbEntryBinding dg,
        final ObjectFactory objectFactory,
        final SafeboxTemplateItem sourceItem,
        final int position,
        final List<String> fieldNames) {
        final String title = sourceItem.title();
        final String suffix = fieldNameConverter.convert(title, fieldNames);
        switch (sourceItem.type()) {
            case SFT_DATE: {
                addStringProperty(dg, objectFactory, title, suffix, false);
                addStringProperty(dg, objectFactory, "_etm_type_" + suffix, "Date Time", false);
                addStringProperty(dg, objectFactory, "_etm_options_" + suffix, null, false);
                break;
            }
            case SFT_MONTH_YEAR: {
                addStringProperty(dg, objectFactory, "_etm_title_" + suffix, title, false);
                addStringProperty(dg, objectFactory, "_etm_type_" + suffix, "Inline", false);
                addStringProperty(dg, objectFactory, "_etm_options_" + suffix, null, false);
                break;
            }
            case SFT_NUMBER: {
                addStringProperty(dg, objectFactory, "_etm_title_" + suffix, title, false);
                addStringProperty(dg,
                        objectFactory,
                        "_etm_type_" + suffix,
                        sourceItem.visible() ? "Inline" : "Protected Inline",
                        false);
                addStringProperty(dg, objectFactory, "_etm_options_" + suffix, null, false);
                break;
            }
            case SFT_PHONE: {
                addStringProperty(dg, objectFactory, "_etm_title_" + suffix, title, false);
                addStringProperty(dg, objectFactory, "_etm_type_" + suffix, "Inline", false);
                addStringProperty(dg, objectFactory, "_etm_options_" + suffix, null, false);
                break;
            }
            case SFT_STRING: {
                addStringProperty(dg, objectFactory, "_etm_title_" + suffix, title, false);
                addStringProperty(dg,
                        objectFactory,
                        "_etm_type_" + suffix,
                        sourceItem.visible() ? "Inline" : "Protected Inline",
                        false);
                addStringProperty(dg, objectFactory, "_etm_options_" + suffix, null, false);
                break;
            }
            case SFT_TEXT: {
                addStringProperty(dg, objectFactory, "_etm_title_" + suffix, title, false);
                addStringProperty(dg,
                        objectFactory,
                        "_etm_type_" + suffix,
                        sourceItem.visible() ? "Inline" : "Protected Inline",
                        false);
                final String textLinesNumber = String.valueOf(properties.getTextLinesNumber());
                addStringProperty(dg, objectFactory, "_etm_options_" + suffix, textLinesNumber, false);
                break;
            }
            case SFT_URI: {
                addStringProperty(dg, objectFactory, "_etm_title_" + suffix, title, false);
                addStringProperty(dg, objectFactory, "_etm_type_" + suffix, "Inline URL", false);
                addStringProperty(dg, objectFactory, "_etm_options_" + suffix, null, false);
                break;
            }
        }
        addStringProperty(dg, objectFactory, "_etm_position_" + suffix, String.valueOf(position), false);

        if (!NULL_UUID.equals(sourceItem.id())) {
            final CustomData.Item oldIdItem = new CustomData.Item();
            oldIdItem.setKey("originalSafeboxItemId_" + suffix);
            oldIdItem.setValue(sourceItem.id().toString());

            CustomData customData = dg.getCustomData();
            if (customData == null) {
                customData = objectFactory.createCustomData();
                dg.setCustomData(customData);
            }
            customData.getItem().add(oldIdItem);
        }
    }
}
