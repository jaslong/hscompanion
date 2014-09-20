package com.jaslong.hscompanion.card;

import com.jaslong.util.android.database.Column;

public final class Util {

    public static <E extends Enum<E>> E toEnum(java.lang.Class<E> cls, String enumName) {
        if (enumName == null) {
            return null;
        } else {
            for (E value : cls.getEnumConstants()) {
                if (value.toString().equalsIgnoreCase(enumName)) {
                    return value;
                }
            }
            return null;
        }
    }

    public static String nameOf(Column column) {
        return column.getDescription().getName();
    }

}
