package com.jaslong.hscompanion.card;

import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.DeckCard;

public class DeckCardImpl implements DeckCard {

    private final long mDeckId;
    private final Card mCard;
    private int mCount;

    public DeckCardImpl(long deckId, Card card, int count) {
        mDeckId = deckId;
        mCard = card;
        mCount = count;
    }

    @Override
    public long getDeckId() {
        return mDeckId;
    }

    @Override
    public Card getCard() {
        return mCard;
    }

    @Override
    public int getCount() {
        return mCount;
    }

}
