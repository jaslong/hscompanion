package com.jaslong.hscompanion.card;

import android.content.ContentValues;

import com.jaslong.hscompanion.database.DeckColumn;
import com.jaslong.hscompanion.model.Deck;
import com.jaslong.hscompanion.model.HeroClass;

class ContentValuesDeck implements Deck {

    private final ContentValues mValues;

    public ContentValuesDeck(ContentValues values) {
        mValues = new ContentValues(values);
    }

    @Override
    public long getId() {
        return mValues.getAsLong(Util.nameOf(DeckColumn.ID));
    }

    @Override
    public String getName() {
        return mValues.getAsString(Util.nameOf(DeckColumn.NAME));
    }

    @Override
    public HeroClass getDeckClass() {
        return Util.toEnum(HeroClass.class,
                mValues.getAsString(Util.nameOf(DeckColumn.HERO_CLASS)));
    }

}
