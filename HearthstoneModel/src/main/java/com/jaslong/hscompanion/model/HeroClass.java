package com.jaslong.hscompanion.model;

/**
 * Represents a class in Hearthstone.
 */
public enum HeroClass {
    ALL_CLASSES("All Classes"),
    DRUID("Druid"),
    HUNTER("Hunter"),
    MAGE("Mage"),
    PALADIN("Paladin"),
    PRIEST("Priest"),
    ROGUE("Rogue"),
    SHAMAN("Shaman"),
    WARLOCK("Warlock"),
    WARRIOR("Warrior");
    private final String mName;
    private HeroClass(String name) {
        mName = name;
    }
    @Override
    public String toString() {
        return mName;
    }
}
