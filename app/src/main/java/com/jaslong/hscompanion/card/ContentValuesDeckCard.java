package com.jaslong.hscompanion.card;

import android.content.ContentValues;

import com.jaslong.hscompanion.database.DeckCardColumn;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.DeckCard;

class ContentValuesDeckCard implements DeckCard {

    private final ContentValues mValues;
    private final Card mCard;

    public ContentValuesDeckCard(ContentValues values) {
        mValues = new ContentValues(values);
        mCard = new ContentValuesCard(values);
    }

    @Override
    public long getDeckId() {
        return mValues.getAsLong(Util.nameOf(DeckCardColumn.DECK_ID));
    }

    @Override
    public Card getCard() {
        return mCard;
    }

    @Override
    public int getCount() {
        return mValues.getAsInteger(Util.nameOf(DeckCardColumn.COUNT));
    }

}
