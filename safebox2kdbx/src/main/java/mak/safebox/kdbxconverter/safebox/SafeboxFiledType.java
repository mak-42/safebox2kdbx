package mak.safebox.kdbxconverter.safebox;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Safebox field type.
 */
public enum SafeboxFiledType {

    /**
     * String.
     */
    SFT_STRING(1),

    /**
     * Multiline text.
     */
    SFT_TEXT(2),

    /**
     * Number.
     */
    SFT_NUMBER(3),

    /**
     * Date.
     */
    SFT_DATE(4),

    /**
     * URI.
     */
    SFT_URI(5),

    /**
     * Phone.
     */
    SFT_PHONE(6),

    /**
     * Month and year.
     */
    SFT_MONTH_YEAR(7);

    /**
     * A map that associates each integer type code with the corresponding enum constant.
     * <p/>
     * Used for quick lookup of enum values by their numeric type codes.
     */
    private static final Map<Integer, SafeboxFiledType> TYPE_2_VALUE = Arrays.stream(SafeboxFiledType.values())
            .collect(Collectors.toMap(SafeboxFiledType::getType, Function.identity()));

    /**
     * Safebox field type code.
     */
    private final int type;

    /**
     * Constructor.
     *
     * @param type
     *            Safebox field type code
     */
    SafeboxFiledType(final int type) {
        this.type = type;
    }

    /**
     * Returns Safebox field type code.
     * 
     * @return Safebox field type code
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the {@link SafeboxFiledType} corresponding to the given integer type code.
     *
     * @param type
     *            the Safebox field type code
     * @return the corresponding enum constant
     * @throws IllegalArgumentException
     *             if the provided type code has no corresponding enum constant
     */
    public static SafeboxFiledType valueOf(final int type) {
        return Optional.ofNullable(TYPE_2_VALUE.get(type))
                .orElseThrow(() -> new IllegalArgumentException("Illegal type: " + type));
    }
}
