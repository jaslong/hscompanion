package com.jaslong.hscompanion.card;

import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.HeroClass;

import java.util.Comparator;

/**
 * Comparator that sorts cards by class, mana cost, and name.
 */
public class CardComparator implements Comparator<Card> {

    @Override
    public int compare(Card lhs, Card rhs) {
        int diff = compareHeroClass(lhs.getHeroClass(), rhs.getHeroClass());
        if (diff != 0) {
            return diff;
        }

        diff = lhs.getManaCost() - rhs.getManaCost();
        if (diff != 0) {
            return diff;
        }

        return lhs.getName().compareTo(rhs.getName());
    }

    private int compareHeroClass(HeroClass lhs, HeroClass rhs) {
        if (lhs == rhs) {
            return 0;
        } else if (lhs == null || lhs == HeroClass.ALL_CLASSES) {
            return 1;
        } else if (rhs == null || rhs == HeroClass.ALL_CLASSES) {
            return -1;
        } else {
            return lhs.toString().compareTo(rhs.toString());
        }
    }

}
