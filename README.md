# Safebox2KDBX
1. [Русский](#русский)
1. [English](#english)

## Русский

### Назначение

Приложение предназначено для переноса базы зашифрованных паролей и зашифрованных заметок из формата Safebox (Сейф) в KDBX.

Поддерживаются преобразование следующих сущностей:
1. Иконок (преобразуются только использованные в карточках и папках).
1. Шаблонов (в формате [KPEntryTemplates](https://github.com/mitchcapper/KPEntryTemplates)). 
1. Папок.
1. Карточек (с привязкой к шаблону).
1. Файлов (привязанные к папкам преобразуются в карточки с вложением, привязанные к карточкам преобразуются в их вложения).

При преобразовании шаблонов и карточек производится попытка использовать стандартные поля KDBX карточек.

При преобразовании папок некоторые из них попадают в группу осиротевших. Такое случается, если у папки нет родителя. Есть подозрение, что это остатки изначального механизма удаления папок, но полной уверенности в этом нет.


### Предусловия

1. На ПК должена быть установлена JRE 21.

### Получение исполняемого файла

Исполняемый файл следует скачать из артефактов последнего релиза: [safebox2kdbx-x.x.x.jar](https://github.com/mak-42/safebox2kdbx/releases/latest).

### Параметры запуска

#### Обязательные параметры

При запуске следует задать следующие параметры:

| Наименование | Тип | Описание |
|--------------|-----|----------|
| converter.source-path | Строка | Путь к базе Safebox, данные из которой будут переноситься. |
| converter.source-password | Строка | Пароль базы Safebox, данные из которой будут переноситься. |
| converter.target-path | Строка | Путь к KDBX-файлу, куда будут помещаться данные. Если файла нет, он будет создан. |
| converter.target-password | Строка | Пароль KDBX-файла, куда будут помещаться данные. |

Пример команды запуска:

```bash
java -Dconverter.source-path=./safabox -Dconverter.source-password=qwerty -Dconverter.target-path=mypasswords.kdbx -Dconverter.target-password=Powell-keepers-could-students -jar safebox2kdbx-1.0.0.jar
```

**Внимание!** Из-за особенностей применённой библиотеки работы с KDBX ([KeePassJava2](https://github.com/jorabin/KeePassJava2)) рекомендуется использовать, либо преобразование в новый KDBX-файл, либо преобразование в созданный ранее пустой KDBX-файл. Эксперименты показали, что даже если конвертер не создаёт ни одной записи, после каждого следующего сохранения размер KDBX-файла увеличивается.

#### Расширенные параметры

При необходимости можно переопределить несколько дополнительных параметров, значение которых задано в приложении:

| Наименование | Тип | Описание |
|--------------|-----|----------|
| converter.orphaned.orphaned-group-icon | Число | Иконка группы осиротевших папок.<br/><br/>По умолчанию: 59 |
| converter.orphaned.orphaned-group-name | Строка | Наименование группы осиротевших папок.<br/><br/>По умолчанию: Orphaned |
| converter.field.name.password | Множество строк | Наименование полей, которые (первое по порядку, если подходящих полей в карточке несколько) будут преобразованы в стандартное поле пароля.<br/><br/>По умолчанию заполнены поля из стандартных шаблонов на разных языках. |
| converter.field.name.url | Множество строк | Наименование полей, которые (первое по порядку, если подходящих полей в карточке несколько) будут преобразованы в стандартное поле url.<br/><br/>По умолчанию заполнены поля из стандартных шаблонов на разных языках. |
| converter.field.name.username | Множество строк | Наименование полей, которые (первое по порядку, если подходящих полей в карточке несколько) будут преобразованы в стандартное поле имени пользователя.<br/><br/>По умолчанию заполнены поля из стандартных шаблонов на разных языках. |

## English

### Purpose

The application is designed to migrate an encrypted password database and encrypted notes from the Safebox format to KDBX.

The following entities are supported for conversion:
1. Icons (used in cards and folders only).
1. Templates (in [KPEntryTemplates](https://github.com/mitchcapper/KPEntryTemplates) format).
1. Folders.
1. Cards (linked to templates).
1. Files (those linked to folders are converted into cards with attachments, those linked to cards become their attachments).

During template and card conversion, it tries to use standard KDBX card fields.

When converting folders, some of them may end up in the "orphaned" group. This occurs if a folder has no parent. It is suspected that this stems from the original folder deletion mechanism, but there is no absolute certainty about this.

## Prerequisites

1. JRE 21 should be installed on the PC.

### Obtaining the Executable File

The executable file should be downloaded from the artifacts of the latest release: [safebox2kdbx-x.x.x.jar](https://github.com/mak-42/safebox2kdbx/releases/latest).

### Launch Parameters

#### Required Parameters

The following parameters must be specified:

| Parameter Name | Type | Description |
|----------------|------|-------------|
| converter.source-path | String | Path to the Safebox database from which data will be migrated. |
| converter.source-password | String | Password for the Safebox database from which data will be migrated. |
| converter.target-path | String | Path to the target KDBX file where data will be placed. If the file does not exist, it will be created. |
| converter.target-password | String | Password for the target KDBX file. |

Example launch command:

```bash
java -Dconverter.source-path=./safabox -Dconverter.source-password=qwerty -Dconverter.target-path=mypasswords.kdbx -Dconverter.target-password=Powell-keepers-could-students -jar safebox2kdbx-1.0.0.jar
```

**Note!** Due to characteristics of the KDBX library used ([KeePassJava2](https://github.com/jorabin/KeePassJava2)), it is recommended to convert to a new KDBX file or convert into an empty KDBX file. Experiments have shown that even if the converter creates no entries, the size of the KDBX file increases after each subsequent save.

#### Advanced Parameters

Several additional parameters whose values are predefined in the application can be overridden:

| Parameter Name | Type | Description |
|----------------|------|-------------|
| converter.orphaned.orphaned-group-icon | Number | Icon for the orphaned folders group.<br/><br/>Default: 59 |
| converter.orphaned.orphaned-group-name | String | Name for the orphaned folders group.<br/><br/>Default: Orphaned |
| converter.field.name.password | Set of Strings | Names of fields which will be mapped (the first matching one if multiple matching fields exist) to the standard password field.<br/><br/>By default populated with field names from standard templates in different languages. |
| converter.field.name.url | Set of Strings | Names of fields which will be mapped (the first matching one if multiple matching fields exist) to the standard URL field.<br/><br/>By default populated with field names from standard templates in different languages. |
| converter.field.name.username | Set of Strings | Names of fields which will be mapped (the first matching one if multiple matching fields exist) to the standard username field.<br/><br/>By default populated with field names from standard templates in different languages. |
