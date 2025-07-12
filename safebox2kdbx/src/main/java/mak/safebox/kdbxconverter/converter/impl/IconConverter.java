package mak.safebox.kdbxconverter.converter.impl;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.linguafranca.pwdb.kdbx.jaxb.binding.CustomIcons;
import org.linguafranca.pwdb.kdbx.jaxb.binding.CustomIcons.Icon;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mak.safebox.kdbxconverter.safebox.SafeboxDataProvider;

/**
 * Converter for Safabox database icons to KDBX icons.
 */
@Service
@Slf4j
@RequiredArgsConstructor
class IconConverter {

    /**
     * Default year.
     */
    private static final int DEFAULT_YEAR = 2000;

    /**
     * Default The moment the icon was last modified..
     */
    private static final Instant DEFAULT_ICON_MOMENT =
            LocalDate.of(DEFAULT_YEAR, 1, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();

    /**
     * Standard icon IDs.
     */
    private static final List<String> ICON_IDS = List.of("551f4e4c-77a9-44a3-a0bd-f92366046c38",
            "362653b1-c493-4dea-ad16-31ae4f4baef2",
            "70592b85-c336-47f7-941c-c098eb6e1b20",
            "24c3ba9e-1a8f-44ea-aae2-de7f4e769c1a",
            "48819ff8-8013-47df-974b-839cdb8896a8",
            "ec9703f2-5e2d-4b02-9c9f-c094dfa43a00",
            "da752dac-27f5-4b95-b377-f05e5fe723b5",
            "cc927dc9-8f5d-492d-9379-1b576796ef5c",
            "4838623b-6684-43d2-b9c6-b0cab9d3b5a3",
            "c7ad7295-02c0-4532-95c1-276006018dc1",
            "d2303b03-b66d-498a-81f6-4d7d1ef0a57b",
            "2ce6ccfd-0905-4449-9d77-b79a9b1b8932",
            "6463ce24-d545-4ea8-a52b-314b462e7aea",
            "4b31ff43-179b-4156-b513-f55c563c0c83",
            "07b34f1d-7c95-494b-9c47-4cf400b54d80",
            "3aebcf96-a21e-405d-8290-6be54311b40e",
            "76e62da8-389b-4aff-80d3-593c336c2dde",
            "98777502-0155-4f80-b65d-7ba7c49ec557",
            "bb7215a6-33fe-47ba-83a0-21810ff38acc",
            "37cc94e3-1a07-4f18-a86b-f2714a22f5d6",
            "4964b0e3-bfa8-48f0-91df-bf4bf9a7355d",
            "54141756-7391-4f5c-9cb2-c357f32ce438",
            "080bcd3e-179e-4fbe-aef2-65abbe420525",
            "24dd4e53-be5c-4630-8fe3-0a1dcb83e92f",
            "95b05800-dced-4368-9336-6578a274f7b4",
            "d28204d3-4537-4c01-bd1c-da89e1b5d74b",
            "57fc9701-2265-4610-8abf-e2063b6ea905",
            "a304f4f6-74ca-49ea-b0e7-74d3334da275",
            "175b678b-2077-4348-af4d-afec9d6413b6",
            "5fa76e9a-ef4e-43ae-a53b-94f519c48249",
            "31186c27-6b5c-42d6-9189-214e59c934b0",
            "4a383098-abfa-4090-bc72-c29d18c64d1a",
            "aba0b6b1-ba38-43fd-b78f-c6cc3d1e4ddd",
            "6532c38b-0be4-41f0-87de-053522f6bd9c",
            "962dd11c-b133-4b2d-af50-396b38427210",
            "c0de2c28-26e3-436d-b7c5-75336f142fca",
            "1eab4967-3884-493a-baa4-e3582db15007",
            "685bec14-e510-4c1b-985b-a579d561cb5e",
            "5b5dc171-83d2-4bfd-acbd-87b473edd478",
            "d6da5341-d00d-405b-8cbf-fc66a7f68a27",
            "a73bfed8-8949-47d0-aebd-26350562585c",
            "8fbbb0cf-cde8-421b-a265-ec9c0960537b",
            "cc096ea2-031f-470c-bca7-52551990d87e",
            "b44fabd5-5a75-4f30-a282-787536a1a329",
            "309f3a69-a5d2-4717-a310-5d8a20a466b4",
            "ca511528-96a9-437f-be9d-e7e0c4506e86",
            "03afca93-6be8-4c52-9014-37995354d604",
            "314c547e-1c2a-4e50-a422-5e3cb5968bb9",
            "67f88f94-1685-4dbf-ade5-736aeb71209f",
            "710ea319-cdc6-4a69-be53-6d0a5b1fc7ae",
            "1e519b59-b307-472b-9691-930f06652845",
            "ad2d21d4-a961-496d-8676-b21c1ef5f761",
            "c8b237da-05b3-413e-a6ba-c3dc77b8ca22",
            "714e10c9-ca91-461a-a49d-67d3b432f772",
            "31231d9a-46ba-4d74-a7ce-f83f066429b3",
            "9217ad27-3502-4b5f-9d68-8e790550db24",
            "59dbf88e-b15a-424c-a14d-a009abb99ae3",
            "a37728e1-7e21-45c2-8146-aacab981c3be",
            "81695a71-a5f3-4fef-b9ff-25d087aad5b2",
            "e62c0870-0c8d-4cc7-b353-65ea2bdddf89",
            "85f77db4-fd49-4aae-bc3e-2bb144fb8f66",
            "e11a7780-22f4-4733-92bc-50ddb5b09f38",
            "7c1869da-b44d-4b84-9c9d-c388aafc246f",
            "ec35cd00-acdf-476b-9a84-3b98eb6ca28e",
            "073413d9-0387-492a-af3d-5f8d25c8c321",
            "fc18e7e9-3181-4cc7-8c3d-ca4f27c91a2d",
            "4b06006f-d235-43af-8e8c-d7ad49fe58c5",
            "71050fa9-3d40-498a-86a7-9113c81899a2",
            "8cb3c4c4-a64f-4895-b6c8-f8d1419df684",
            "77c7170a-8ccb-4941-acc9-669e82bb94f6",
            "3eeec813-3692-431a-be90-48a6fced25fd",
            "f927e6d5-1c3d-4952-b8a3-4fff4b18d093",
            "d796ee33-9ff7-4712-afee-a58cd9bd79a6",
            "8e4c40a9-8216-4277-972e-b1220d4dfb0c",
            "234d03d8-955e-46f4-bda5-ccf9b8de4de5",
            "f3404d21-c678-491b-9e05-f12cce822901",
            "3e8e1e33-0851-48ac-980c-a086f5dfb795",
            "43efd820-77cd-4527-ba1b-9e0a5869aa33",
            "cd2576b1-804b-4362-a1c2-2eb6958ecbdd",
            "0b89acbf-760f-47be-bd84-2a21fd242838",
            "f9ce87f8-aeb9-4172-897c-14d27a75d291",
            "55320e06-653c-44d7-938a-055b200065f8",
            "96d86898-225d-4a10-99a1-97222ef7cc31",
            "de811260-27c9-4fff-95b2-1cc2aa027916",
            "f8643d59-de11-434c-8a79-c87a7d236ee7",
            "bec7e679-7626-4b74-859d-f5f4f28c15a3",
            "12fdc36e-baf8-444f-af1c-797ba5a87854",
            "93f90121-c891-43e7-9827-f81f0b731916",
            "994b3a2a-3f57-4828-96c6-98b10d3604bd",
            "9380fb90-bd2e-4444-babd-066f674d46c1",
            "11111111-1111-1111-1111-111111111111");

    /**
     * Standard icon resource names.
     */
    private static final List<String> ICON_NAMES = List.of("icons_accounts.png",
            "icons_bag.png",
            "icons_bank.png",
            "icons_bank2.png",
            "icons_books.png",
            "icons_boy.png",
            "icons_calendar.png",
            "icons_car.png",
            "icons_cart.png",
            "icons_case.png",
            "icons_case2.png",
            "icons_case3.png",
            "icons_cat.png",
            "icons_certificate.png",
            "icons_certificate2.png",
            "icons_chat.png",
            "icons_console.png",
            "icons_console2.png",
            "icons_creditcard.png",
            "icons_creditcards.png",
            "icons_dices.png",
            "icons_dog.png",
            "icons_exthdd.png",
            "icons_exthdd2.png",
            "icons_extstorage.png",
            "icons_extstorage2.png",
            "icons_family.png",
            "icons_favourite.png",
            "icons_flash.png",
            "icons_folder.png",
            "icons_folder2.png",
            "icons_food.png",
            "icons_food2.png",
            "icons_gift.png",
            "icons_girl.png",
            "icons_home.png",
            "icons_icq.png",
            "icons_install.png",
            "icons_install2.png",
            "icons_insurance.png",
            "icons_lamp.png",
            "icons_lock.png",
            "icons_lock2.png",
            "icons_love.png",
            "icons_love2.png",
            "icons_mail.png",
            "icons_mail2.png",
            "icons_mail3.png",
            "icons_man.png",
            "icons_map.png",
            "icons_medical.png",
            "icons_medical2.png",
            "icons_messenger.png",
            "icons_mfu.png",
            "icons_misc.png",
            "icons_money.png",
            "icons_note.png",
            "icons_notebook.png",
            "icons_passport.png",
            "icons_phone.png",
            "icons_phone2.png",
            "icons_photo.png",
            "icons_physical.png",
            "icons_place.png",
            "icons_safe.png",
            "icons_security.png",
            "icons_server.png",
            "icons_server2.png",
            "icons_stock.png",
            "icons_strawberries.png",
            "icons_sysmonitor.png",
            "icons_text.png",
            "icons_tickets.png",
            "icons_web.png",
            "icons_web2.png",
            "icons_webcam.png",
            "icons_webcam2.png",
            "icons_wifi.png",
            "icons_wireless.png",
            "icons_woman.png",
            "icons_image.png",
            "icons_headphones.png",
            "icons_microphone.png",
            "icons_videocamera.png",
            "icons_film.png",
            "icons_clip.png",
            "icons_database.png",
            "icons_scanner.png",
            "icons_webmoney.png",
            "icons_airplane.png",
            "icons_install.png");

    /**
     * The application resource loader.
     */
    private final ResourceLoader resourceLoader;

    /**
     * Creates a map that associates icon IDs with their corresponding info of icon objects.
     * 
     * @param sourceDataProvider
     *            Safebox data provider
     * @return created map that associates icon IDs with their corresponding info of icon objects
     */
    public Map<UUID, IconInfo> createId2IconInfo(final SafeboxDataProvider sourceDataProvider) {
        final int stndardIconsSize = Math.max(ICON_IDS.size(), ICON_NAMES.size());
        @SuppressWarnings("unchecked")
        final Map<UUID, IconInfo> result = Map.ofEntries((Entry<UUID, IconInfo>[]) Stream
                .concat(IntStream.iterate(0, i -> i < stndardIconsSize, i -> i + 1).mapToObj(i -> {
                    final String filename = ICON_NAMES.get(i);
                    final String name = FilenameUtils.getBaseName(filename);
                    return Map.entry(UUID.fromString(ICON_IDS.get(i)),
                            new IconInfo(name,
                                    resourceLoader.getResource("classpath:/icons/" + filename),
                                    DEFAULT_ICON_MOMENT));
                }),
                        sourceDataProvider.icons()
                                .map(icon -> Map.entry(icon.id(),
                                        new IconInfo(icon.fileName().toString(),
                                                new ByteArrayResource(icon.content()),
                                                icon.modifiedAt()))))
                .toArray(Map.Entry[]::new));

        return result;
    }

    /**
     * Converts the icon if it has not been already converted.
     * 
     * @param iconId
     *            the ID of the icon
     * @param context
     *            the context used during the conversion process
     */
    public void processIconIfRequired(final UUID iconId, final Context context) {
        final IconInfo iconInfo = context.id2IconInfo().get(iconId);
        if (iconInfo == null) {
            throw new ContextedRuntimeException("The icon is not defined in the source database")
                    .setContextValue("iconId", iconId);
        }
        if (iconInfo.isConverted()) {
            return;
        }

        final List<Icon> targetIcons = context.targetDb().getKeePassFile().getMeta().getCustomIcons().getIcon();
        if (targetIcons.stream().anyMatch(icon -> iconId.equals(icon.getUUID()))) {
            LOG.info("Icon was converted before and been skipped: id={}, title={}", iconId, iconInfo.getName());
            iconInfo.setConverted(true);
            return;
        }

        iconInfo.getName();
        final CustomIcons.Icon targetIcon = new CustomIcons.Icon();
        targetIcon.setName(iconInfo.getName());
        targetIcon.setUUID(iconId);
        try {
            targetIcon.setData(iconInfo.getContent().getContentAsByteArray());
        } catch (final IOException exception) {
            throw new ContextedRuntimeException("Unable to read icon content").setContextValue("iconInfo", iconInfo);
        }
        targetIcon.setLastModificationTime(Date.from(iconInfo.getModifiedAt()));
        targetIcons.add(targetIcon);
        iconInfo.setConverted(true);
        LOG.info("Icon has been converted: id={}, name={}", iconId, iconInfo.getName());
    }
}
