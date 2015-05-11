package com.jaslong.hscompanion.model;

/**
 * Represents a card in Hearthstone.
 */
public interface Card {

    public enum Race {
        BEAST, DEMON, DRAGON, MECH, MURLOC, PIRATE, TOTEM
    }

    public enum Rarity {
        FREE("Free"), COMMON("Common"), RARE("Rare"), EPIC("Epic"), LEGENDARY("Legendary");
        private final String mName;
        private Rarity(String name) {
            mName = name;
        }
        @Override
        public String toString() {
            return mName;
        }
    }

    public enum Set {
        BASIC("Basic"),
        EXPERT("Expert"),
        REWARD("Reward"),
        PROMO("Promo"),
        NAXXRAMAS("Naxxramas"),
        GOBLINS_VS_GNOMES("Goblins vs Gnomes"),
        BLACKROCK_MOUNTAIN("Blackrock Mountain");
        private final String mName;
        private Set(String name) {
            mName = name;
        }
        @Override
        public String toString() {
            return mName;
        }
    }

    public enum Type {
        MINION, SPELL, WEAPON
    }

    String getArtistName();
    Integer getAttack();
    HeroClass getHeroClass();
    String getCardId();
    String getName();
    Set getSet();
    String getText();
    Type getType();
    boolean isCollectible();
    int getManaCost();
    Integer getDurability();
    String getFlavorText();
    Integer getHealth();
    String getHowToGetThisCard();
    String getHowToGetThisGoldCard();
    String getImageUri();
    Race getRace();
    Rarity getRarity();
    boolean isSecret();

}
