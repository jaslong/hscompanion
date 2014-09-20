package com.jaslong.hscompanion.model;

/**
 * Represents the relationship of a card in a deck.
 */
public interface DeckCard {

    long getDeckId();
    Card getCard();
    int getCount();

}
