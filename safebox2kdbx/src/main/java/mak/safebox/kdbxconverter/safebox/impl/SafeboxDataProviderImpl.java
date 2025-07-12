package mak.safebox.kdbxconverter.safebox.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ContextedRuntimeException;
import org.apache.commons.lang3.function.FailableConsumer;
import org.springframework.lang.Nullable;

import mak.safebox.kdbxconverter.safebox.SafeboxCard;
import mak.safebox.kdbxconverter.safebox.SafeboxCardField;
import mak.safebox.kdbxconverter.safebox.SafeboxDataProvider;
import mak.safebox.kdbxconverter.safebox.SafeboxFile;
import mak.safebox.kdbxconverter.safebox.SafeboxFiledType;
import mak.safebox.kdbxconverter.safebox.SafeboxFolder;
import mak.safebox.kdbxconverter.safebox.SafeboxIcon;
import mak.safebox.kdbxconverter.safebox.SafeboxTemplate;
import mak.safebox.kdbxconverter.safebox.SafeboxTemplateItem;

/**
 * The Safebox data provider implementation.
 */
class SafeboxDataProviderImpl implements SafeboxDataProvider {

    /**
     * Represents the converter from ResultSet into a Safebox entity.
     * 
     * @param <T>
     *            type of Safebox entity
     */
    @FunctionalInterface
    private interface RsConverter<T> {

        /**
         * Converts row from ResultSet into a Safebox entity.
         * 
         * @param rs
         *            the ResultSet
         * @return converted Safebox entity
         * @throws SQLException
         *             if Safebox database access error
         */
        T convert(ResultSet rs) throws SQLException;
    }

    /**
     * NULL ID.
     */
    private static final String NULL_ID = "00000000-0000-0000-0000-000000000000";

    /**
     * The parent ID of orphaned folders.
     */
    private static final String ORPHANED_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";

    /**
     * The connection to Safebox database.
     */
    private final Connection connection;

    /**
     * Safebox data decrypter.
     */
    private final DataDecrypter decrypter;

    /**
     * Safebox icon decrypter.
     */
    private final IconDecrypter iconDecrypter;

    /**
     * Safebox file decrypter.
     */
    private final FileDecrypter fileDecrypter;

    /**
     * Constructor.
     * 
     * @param connection
     *            the connection to Safebox database
     * @param decrypter
     *            Safebox data decrypter
     * @param iconDecrypter
     *            Safebox icon decrypter
     * @param fileDecrypter
     *            Safebox file decrypter
     */
    SafeboxDataProviderImpl(final Connection connection,
        final DataDecrypter decrypter,
        final IconDecrypter iconDecrypter,
        final FileDecrypter fileDecrypter) {
        this.connection = connection;
        this.decrypter = decrypter;
        this.iconDecrypter = iconDecrypter;
        this.fileDecrypter = fileDecrypter;

        try (ResultSet rs = connection.createStatement().executeQuery("select checksum from safebox_init limit 1")) {
            if (!(rs.next() && (decrypter.decryptString(rs.getBytes("checksum")) != null))) {
                throw new RuntimeException("It looks like the password is wrong");
            }
        } catch (final SQLException exception) {
            throw new RuntimeException("Unable to check password", exception);
        }
    }

    @Override
    public Stream<SafeboxTemplate> templates() {
        return stream("select * from safebox_templates where isdeleted=0 order by _id", rs -> {

            final String rawTemplateId = rs.getString("rid");
            final List<SafeboxTemplateItem> items =
                    stream("select * from safebox_items where template_rid=? and isdeleted=0 order by sequence",
                            statement -> statement.setString(1, rawTemplateId),
                            irs -> new SafeboxTemplateItem(UUID.fromString(irs.getString("rid")),
                                    decrypter.decryptString(irs.getBytes("title")),
                                    SafeboxFiledType.valueOf(irs.getInt("type")),
                                    irs.getBoolean("visibility"),
                                    Instant.ofEpochMilli(irs.getDate("created").getTime()),
                                    Instant.ofEpochMilli(irs.getDate("modified").getTime())))
                                            .collect(Collectors.toList());
            return new SafeboxTemplate(UUID.fromString(rawTemplateId),
                    UUID.fromString(rs.getString("icon_rid")),
                    decrypter.decryptString(rs.getBytes("title")),
                    decrypter.decryptString(rs.getBytes("description")),
                    items,
                    Instant.ofEpochMilli(rs.getDate("created").getTime()),
                    Instant.ofEpochMilli(rs.getDate("modified").getTime()));
        });
    }

    @Override
    public Stream<SafeboxIcon> icons() {
        return stream("select * from safebox_icons where isdeleted=0 order by _id", rs -> {
            final UUID iconFileName = UUID.fromString(decrypter.decryptString(rs.getBytes("uuid")));
            return new SafeboxIcon(UUID.fromString(rs.getString("rid")),
                    iconFileName,
                    rs.getString("hash"),
                    Instant.ofEpochMilli(rs.getDate("created").getTime()),
                    Instant.ofEpochMilli(rs.getDate("modified").getTime()),
                    iconDecrypter.decrypt(iconFileName));
        });
    }

    @Override
    public Stream<SafeboxFolder> folders(@Nullable
    final UUID parentId) {
        final boolean root = parentId == null;
        return innerFolders(root ? NULL_ID : parentId.toString(), root);
    }

    @Override
    public Stream<SafeboxFolder> orphanedFolders() {
        return innerFolders(ORPHANED_ID, true);
    }

    /**
     * Starts the stream of Safebox folders.
     * 
     * @param parentId
     *            ID of parent folder
     * @param root
     *            flag indicating whether the parent is a root
     * @return the stream of Safebox folders
     */
    private Stream<SafeboxFolder> innerFolders(final String parentId, final boolean root) {
        return stream("select * from safebox_folders where parent_rid = ? and isdeleted=0 order by created, _id",
                statement -> statement.setString(1, parentId),
                rs -> {
                    final String rawTemplateId = rs.getString("template_rid");
                    return new SafeboxFolder(UUID.fromString(rs.getString("rid")),
                            root ? null : UUID.fromString(parentId),
                            UUID.fromString(rs.getString("icon_rid")),
                            NULL_ID.equals(rawTemplateId) ? null : UUID.fromString(rawTemplateId),
                            decrypter.decryptString(rs.getBytes("title")),
                            decrypter.decryptString(rs.getBytes("description")),
                            hasColumn(rs, "parameters") ? decrypter.decryptString(rs.getBytes("parameters")) : null,
                            Instant.ofEpochMilli(rs.getDate("created").getTime()),
                            Instant.ofEpochMilli(rs.getDate("modified").getTime()));
                });
    }

    @Override
    public Stream<SafeboxCard> cards(final UUID folderId) {
        return stream("select * from safebox_cards where folder_rid = ? and isdeleted=0 order by _id",
                statement -> statement.setString(1, folderId.toString()),
                rs -> {
                    final String rawCardId = rs.getString("rid");
                    final List<SafeboxCardField> fields = stream("select sv.rid, sv.item_rid, si.title, si.\"type\""
                            + ",sv.value, si.visibility, sv.modified from safebox_values sv"
                            + "    join safebox_items si on sv.item_rid = si.rid"
                            + "    where sv.card_rid=? and sv.isdeleted=0 and si.isdeleted=0"
                            + "    order by si.\"sequence\"", statement -> statement.setString(1, rawCardId), frs -> {
                                final SafeboxFiledType type = SafeboxFiledType.valueOf(frs.getInt("type"));
                                return new SafeboxCardField(UUID.fromString(frs.getString("rid")),
                                        UUID.fromString(frs.getString("item_rid")),
                                        decrypter.decryptString(frs.getBytes("title")),
                                        type,
                                        convertValue(decrypter.decryptString(frs.getBytes("value")), type),
                                        frs.getBoolean("visibility"),
                                        Instant.ofEpochMilli(frs.getDate("modified").getTime()));
                            }).collect(Collectors.toUnmodifiableList());
                    return new SafeboxCard(UUID.fromString(rawCardId),
                            UUID.fromString(rs.getString("folder_rid")),
                            UUID.fromString(rs.getString("template_rid")),
                            UUID.fromString(rs.getString("icon_rid")),
                            decrypter.decryptString(rs.getBytes("title")),
                            decrypter.decryptString(rs.getBytes("description")),
                            rs.getBoolean("isfavourite"),
                            rs.getInt("access_counter"),
                            fields,
                            Instant.ofEpochMilli(rs.getDate("accessed").getTime()),
                            Instant.ofEpochMilli(rs.getDate("created").getTime()),
                            Instant.ofEpochMilli(rs.getDate("modified").getTime()));
                });
    }

    @Override
    public Stream<SafeboxFile> files(final UUID containerId) {
        return stream("select * from safebox_files where folder_rid = ? and isdeleted=0 order by created",
                statement -> statement.setString(1, containerId.toString()),
                rs -> new SafeboxFile(UUID.fromString(rs.getString("rid")),
                        UUID.fromString(rs.getString("icon_rid")),
                        decrypter.decryptString(rs.getBytes("title")),
                        decrypter.decryptString(rs.getBytes("description")),
                        hasColumn(rs, "parameters") ? decrypter.decryptString(rs.getBytes("parameters")) : null,
                        decrypter.decryptString(rs.getBytes("mime_type")),
                        decrypter.decryptString(rs.getBytes("file_name")),
                        Long.parseLong(decrypter.decryptString(rs.getBytes("file_size"))),
                        stringToInstant(decrypter.decryptString(rs.getBytes("file_time"))),
                        rs.getBoolean("isfavourite"),
                        rs.getInt("access_counter"),
                        Instant.ofEpochMilli(rs.getDate("accessed").getTime()),
                        Instant.ofEpochMilli(rs.getDate("created").getTime()),
                        Instant.ofEpochMilli(rs.getDate("modified").getTime()),
                        fileDecrypter.decrypt(UUID.fromString(decrypter.decryptString(rs.getBytes("file_uuid"))))));
    }

    /**
     * Converts a string representation of a Safebox value into its corresponding typed value.
     * 
     * @param value
     *            the string representation of the value
     * @param type
     *            the target type to which the value should be converted
     * @return the typed value, or <code>null</code>, if value is <code>null</code>
     */
    @Nullable
    private Object convertValue(@Nullable
    final String value, final SafeboxFiledType type) {
        if (value == null) {
            return null;
        }
        return switch (type) {
            case SFT_DATE, SFT_MONTH_YEAR -> stringToInstant(value);
            case SFT_NUMBER -> value;
            case SFT_PHONE, SFT_STRING, SFT_TEXT, SFT_URI -> value;
        };
    }

    /**
     * Converts a Safebox date/time string into an {@link Instant}.
     * 
     * @param value
     *            the source Safebox date/time string
     * @return an Instant representing the parsed date/time, or <code>null</code> if the value is <code>null</code>
     */
    @Nullable
    private Instant stringToInstant(@Nullable
    final String value) {
        return StringUtils.isEmpty(value) ? null : Instant.ofEpochMilli(Long.valueOf(value));
    }

    /**
     * Check if the result set has got the column.
     * 
     * @param rs
     *            result set to be checked
     * @param columnName
     *            column name
     * @return <code>true</code> if the result set has got the column, <code>false</code> if the result set has not got
     *         the column
     * @throws SQLException
     *             if something goes wrong
     */
    private boolean hasColumn(final ResultSet rs, final String columnName) throws SQLException {
        final ResultSetMetaData rsmd = rs.getMetaData();
        final int columnsCount = rsmd.getColumnCount();
        for (int i = 1; i <= columnsCount; i++) {
            if (columnName.equals(rsmd.getColumnName(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates new stream of Safebox entities.
     * 
     * @param <T>
     *            type of Safebox entity
     * @param query
     *            SQL statement to be sent to database for selecting data of Safebox entities
     * @param converter
     *            converts a row from ResultSet into a Safebox entity
     * @return new stream of Safebox entities
     */
    private <T> Stream<T> stream(final String query, final RsConverter<T> converter) {
        return stream(query, statement -> {
            // nothing to set
        }, converter);
    }

    /**
     * Creates new stream of Safebox entities.
     * 
     * @param <T>
     *            type of Safebox entity
     * @param query
     *            SQL statement to be sent to database for selecting data of Safebox entities
     * @param queryParamsSetter
     *            provides the parameters of SQL statement
     * @param converter
     *            converts a row from ResultSet into a Safebox entity
     * @return new stream of Safebox entities
     */
    private <T> Stream<T> stream(final String query,
        final FailableConsumer<PreparedStatement, SQLException> queryParamsSetter,
        final RsConverter<T> converter) {
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED) {

            private ResultSet resultSet = null;

            private int row = 0;

            @Override
            public boolean tryAdvance(final Consumer<? super T> action) {
                this.row++;
                try {
                    if (resultSet == null) {
                        final PreparedStatement statement = connection.prepareStatement(query);
                        queryParamsSetter.accept(statement);
                        resultSet = statement.executeQuery();
                    }

                    if (!resultSet.next()) {
                        closeQuietly(resultSet);
                        return false;
                    }
                    action.accept(converter.convert(resultSet));
                } catch (final SQLException exception) {
                    closeQuietly(resultSet);
                    throw new ContextedRuntimeException("Unable to read database", exception)
                            .setContextValue("query", query).setContextValue("row", this.row);
                }

                return true;
            }

        }, false);
    }

    /**
     * Closes a {@link AutoCloseable} unconditionally.
     * 
     * @param closeable
     *            the objects to close, may be null or already closed
     */
    private void closeQuietly(@Nullable
    final AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final Exception exeption) {
                // skip it.
            }
        }
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

}
