package com.jaslong.hscompanion.database;

import com.jaslong.util.android.database.Column;

public enum WordCardColumn implements Column {

    WORD(Type.TEXT, Modifier.NOT_NULL),
    CARD_ID(Type.TEXT, Modifier.NOT_NULL);

    private final Description mDescription;

    private WordCardColumn(Type type, Modifier... modifiers) {
        this(null, type, modifiers);
    }

    private WordCardColumn(String name, Type type, Modifier... modifiers) {
        mDescription = new Description(name != null ? name : name().toLowerCase(), type, modifiers);
    }

    @Override
    public Description getDescription() {
        return mDescription;
    }

}
