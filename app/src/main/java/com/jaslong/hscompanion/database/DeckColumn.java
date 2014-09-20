package com.jaslong.hscompanion.database;

import android.provider.BaseColumns;

import com.jaslong.util.android.database.Column;

public enum DeckColumn implements Column {

    ID(BaseColumns._ID, Type.INTEGER, Modifier.PRIMARY_KEY),
    NAME(Type.TEXT, Modifier.NOT_NULL),
    HERO_CLASS(Type.TEXT, Modifier.NOT_NULL);

    private final Description mDescription;

    private DeckColumn(Type type, Modifier... modifiers) {
        this(null, type, modifiers);
    }

    private DeckColumn(String name, Type type, Modifier... modifiers) {
        mDescription = new Description(name != null ? name : name().toLowerCase(), type, modifiers);
    }

    @Override
    public Description getDescription() {
        return mDescription;
    }

}
