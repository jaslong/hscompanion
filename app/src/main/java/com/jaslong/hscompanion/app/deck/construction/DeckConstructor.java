package com.jaslong.hscompanion.app.deck.construction;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.jaslong.hscompanion.util.Logger;
import com.jaslong.hscompanion.card.CardComparator;
import com.jaslong.hscompanion.card.DeckCardImpl;
import com.jaslong.hscompanion.card.DeckManager;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.DeckCard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class DeckConstructor {

    private static final Logger sLogger = Logger.create("Construction", "DeckConstructor");

    private static final int MAX_DECK_SIZE = 30;
    private static final int MAX_LEGENDARY_CARD_COUNT = 1;
    private static final int MAX_CARD_COUNT = 2;

    private final DeckManager mDeckManager;
    private final long mDeckId;
    private final SortedMap<CardWrapper, Integer> mCardCounts;
    private int mTotalCount;

    public DeckConstructor(DeckManager deckManager, long deckId, List<DeckCard> deckCards) {
        mDeckManager = deckManager;
        mDeckId = deckId;
        mCardCounts = new TreeMap<>();
        mTotalCount = 0;
        for (DeckCard deckCard : deckCards) {
            mCardCounts.put(new CardWrapper(deckCard.getCard()), deckCard.getCount());
            mTotalCount += deckCard.getCount();
        }
    }

    public List<DeckCard> getDeckCards() {
        List<DeckCard> list = new ArrayList<>(mCardCounts.size());
        for (Map.Entry<CardWrapper, Integer> entry : mCardCounts.entrySet()) {
            list.add(new DeckCardImpl(mDeckId, entry.getKey().getCard(), entry.getValue()));
        }
        return list;
    }

    public boolean addCard(final Card card) {
        CardWrapper wrapper = new CardWrapper(card);

        Integer count = mCardCounts.get(wrapper);
        if (count == null) {
            count = 0;
        }

        if ((card.getRarity() == Card.Rarity.LEGENDARY && count == MAX_LEGENDARY_CARD_COUNT)
                || count == MAX_CARD_COUNT || mTotalCount == MAX_DECK_SIZE) {
            return false;
        }

        ++count;
        ++mTotalCount;
        mCardCounts.put(wrapper, count);

        final int cardCount = count;
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean success = mDeckManager.updateDeckCard(
                        new DeckCardImpl(mDeckId, card, cardCount));
                if (!success) {
                    sLogger.w("Failed to update deck card relationship.");
                }
                return success;
            }
        }.execute();

        return true;
    }

    public boolean removeCard(final Card card) {
        CardWrapper wrapper = new CardWrapper(card);

        Integer count = mCardCounts.get(wrapper);
        if (count == null) {
            sLogger.w("Trying to remove non-existent card from deck.");
            return false;
        } else if (count == 0) {
            sLogger.w("Deck has card with count 0.");
            return false;
        }

        --count;
        --mTotalCount;
        if (count > 0) {
            mCardCounts.put(wrapper, count);
        } else {
            mCardCounts.remove(wrapper);
        }

        final int cardCount = count;
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean success = mDeckManager.removeDeckCard(
                        new DeckCardImpl(mDeckId, card, cardCount));
                if (!success) {
                    sLogger.w("Failed to remove deck card relationship.");
                }
                return success;
            }
        }.execute();

        return true;
    }

    private static class CardWrapper implements Comparable<CardWrapper> {
        private static Comparator<Card> COMPARATOR = new CardComparator();
        private final Card mCard;
        public CardWrapper(Card card) {
            mCard = card;
        }
        public Card getCard() {
            return mCard;
        }
        @Override
        public int hashCode() {
            return mCard.getCardId().hashCode();
        }
        @Override
        public boolean equals(Object o) {
            return o instanceof CardWrapper &&
                    mCard.getCardId().equals(((CardWrapper) o).mCard.getCardId());
        }
        @Override
        public int compareTo(@NonNull CardWrapper another) {
            return COMPARATOR.compare(mCard, another.mCard);
        }
    }

}
