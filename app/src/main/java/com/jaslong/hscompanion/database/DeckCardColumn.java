package com.jaslong.hscompanion.database;

import com.jaslong.util.android.database.Column;

public enum DeckCardColumn implements Column {

    DECK_ID(Type.INTEGER, Modifier.NOT_NULL),
    CARD_ID(Type.TEXT, Modifier.NOT_NULL),
    COUNT(Type.TEXT, Modifier.NOT_NULL);

    private final Description mDescription;

    private DeckCardColumn(Type type, Modifier... modifiers) {
        this(null, type, modifiers);
    }

    private DeckCardColumn(String name, Type type, Modifier... modifiers) {
        mDescription = new Description(name != null ? name : name().toLowerCase(), type, modifiers);
    }

    @Override
    public Description getDescription() {
        return mDescription;
    }

}
