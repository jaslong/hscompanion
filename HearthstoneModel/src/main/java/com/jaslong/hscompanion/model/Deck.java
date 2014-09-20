package com.jaslong.hscompanion.model;

/**
 * Represents a collection of cards that form a deck.
 */
public interface Deck {

    long getId();
    String getName();
    HeroClass getDeckClass();

}
