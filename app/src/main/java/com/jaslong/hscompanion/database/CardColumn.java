package com.jaslong.hscompanion.database;

import android.provider.BaseColumns;

import com.jaslong.util.android.database.Column;

public enum CardColumn implements Column {

    ID(BaseColumns._ID, Type.INTEGER, Modifier.PRIMARY_KEY),
    ARTIST_NAME(Type.TEXT, Modifier.NOT_NULL),
    ATTACK(Type.INTEGER),
    CLASS(Type.TEXT),
    CARD_ID(Type.TEXT, Modifier.NOT_NULL, Modifier.UNIQUE),
    COLLECTIBLE(Type.INTEGER, Modifier.NOT_NULL),
    DURABILITY(Type.INTEGER),
    FLAVOR_TEXT(Type.TEXT),
    HEALTH(Type.INTEGER),
    HOW_TO_GET_THIS_CARD(Type.TEXT),
    HOW_TO_GET_THIS_GOLD_CARD(Type.TEXT),
    MANA_COST(Type.INTEGER, Modifier.NOT_NULL),
    NAME(Type.TEXT, Modifier.NOT_NULL),
    RACE(Type.TEXT),
    RARITY(Type.TEXT, Modifier.NOT_NULL),
    SECRET(Type.INTEGER, Modifier.NOT_NULL),
    SET("card_set", Type.TEXT, Modifier.NOT_NULL),
    TEXT(Type.TEXT),
    TYPE(Type.TEXT, Modifier.NOT_NULL);

    private final Description mDescription;

    private CardColumn(Type type, Modifier... modifiers) {
        this(null, type, modifiers);
    }

    private CardColumn(String name, Type type, Modifier... modifiers) {
        mDescription = new Description(name != null ? name : name().toLowerCase(), type, modifiers);
    }

    @Override
    public Description getDescription() {
        return mDescription;
    }

}
