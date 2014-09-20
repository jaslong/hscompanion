package com.jaslong.hscompanion.util;

import android.content.res.Resources;

import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.HeroClass;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class ColorPicker {

    private static final Map<HeroClass, Integer> HERO_CLASS_COLORS;
    static {
        Map<HeroClass, Integer> colors = new EnumMap<HeroClass, Integer>(HeroClass.class);
        colors.put(HeroClass.DRUID, R.color.druid);
        colors.put(HeroClass.HUNTER, R.color.hunter);
        colors.put(HeroClass.MAGE, R.color.mage);
        colors.put(HeroClass.PALADIN, R.color.paladin);
        colors.put(HeroClass.PRIEST, R.color.priest);
        colors.put(HeroClass.ROGUE, R.color.rogue);
        colors.put(HeroClass.SHAMAN, R.color.shaman);
        colors.put(HeroClass.WARLOCK, R.color.warlock);
        colors.put(HeroClass.WARRIOR, R.color.warrior);
        HERO_CLASS_COLORS = Collections.unmodifiableMap(colors);
    }

    private static final Map<Card.Rarity, Integer> RARITY_COLORS;
    static {
        Map<Card.Rarity, Integer> colors = new EnumMap<Card.Rarity, Integer>(Card.Rarity.class);
        colors.put(Card.Rarity.FREE, R.color.rarity_free);
        colors.put(Card.Rarity.COMMON, R.color.rarity_common);
        colors.put(Card.Rarity.RARE, R.color.rarity_rare);
        colors.put(Card.Rarity.EPIC, R.color.rarity_epic);
        colors.put(Card.Rarity.LEGENDARY, R.color.rarity_legendary);
        RARITY_COLORS = Collections.unmodifiableMap(colors);
    }

    public static int getColor(HeroClass cardClass, Resources resources) {
        Integer colorRes = HERO_CLASS_COLORS.get(cardClass);
        if (colorRes == null) {
            colorRes = R.color.neutral;
        }
        return resources.getColor(colorRes);
    }

    public static int getColor(Card.Rarity rarity, Resources resources) {
        Integer colorRes = RARITY_COLORS.get(rarity);
        if (colorRes == null) {
            colorRes = R.color.rarity_free;
        }
        return resources.getColor(colorRes);
    }

    private ColorPicker() {
        throw new UnsupportedOperationException();
    }

}
