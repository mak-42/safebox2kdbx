package mak.safebox.kdbxconverter.safebox;


import java.sql.Types;

/**
 * The class represents a column description of a source data row.
 */
public final class ColumnDescriptor {

    /**
     * The column name.
     */
    private final String name;

    /**
     * The column type.
     * 
     * @see Types
     */
    private final int type;

    /**
     * <code>true</code> if the field is encrypted, otherwise - <code>false</code>.
     */
    private final boolean encrypted;

    /**
     * Constructor.
     * 
     * @param name
     *            the column name
     * @param type
     *            the column type
     * @param encrypted
     *            <code>true</code> if the field is encrypted, otherwise - <code>false</code>
     * @see Types
     */
    public ColumnDescriptor(final String name, final int type, final boolean encrypted) {
        super();
        this.name = name;
        this.type = type;
        this.encrypted = encrypted;
    }

    /**
     * Returns the column name.
     * 
     * @return the column name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the column type.
     * 
     * @return the column type
     */
    public int getType() {
        return type;
    }

    /**
     * Returns <code>true</code> if the field is encrypted, otherwise - <code>false</code>.
     * 
     * @return <code>true</code> if the field is encrypted, otherwise - <code>false</code>
     */
    public boolean isEncrypted() {
        return encrypted;
    }
}
