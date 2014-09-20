package com.jaslong.hscompanion.card;

import android.content.ContentValues;

import com.jaslong.hscompanion.database.CardColumn;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.HeroClass;

class ContentValuesCard implements Card {

    private final ContentValues mValues;

    public ContentValuesCard(ContentValues values) {
        mValues = new ContentValues(values);
    }

    @Override
    public String getArtistName() {
        return mValues.getAsString(Util.nameOf(CardColumn.ARTIST_NAME));
    }

    @Override
    public Integer getAttack() {
        return mValues.getAsInteger(Util.nameOf(CardColumn.ATTACK));
    }

    @Override
    public HeroClass getHeroClass() {
        return getEnum(HeroClass.class, CardColumn.CLASS);
    }

    @Override
    public String getCardId() {
        return mValues.getAsString(Util.nameOf(CardColumn.CARD_ID));
    }

    @Override
    public String getName() {
        return mValues.getAsString(Util.nameOf(CardColumn.NAME));
    }

    @Override
    public Set getSet() {
        return getEnum(Set.class, CardColumn.SET);
    }

    @Override
    public String getText() {
        return mValues.getAsString(Util.nameOf(CardColumn.TEXT));
    }

    @Override
    public Type getType() {
        return getEnum(Type.class, CardColumn.TYPE);
    }

    @Override
    public boolean isCollectible() {
        return mValues.getAsInteger(Util.nameOf(CardColumn.COLLECTIBLE)) != 0;
    }

    @Override
    public int getManaCost() {
        return mValues.getAsInteger(Util.nameOf(CardColumn.MANA_COST));
    }

    @Override
    public Integer getDurability() {
        return mValues.getAsInteger(Util.nameOf(CardColumn.DURABILITY));
    }

    @Override
    public String getFlavorText() {
        return mValues.getAsString(Util.nameOf(CardColumn.FLAVOR_TEXT));
    }

    @Override
    public Integer getHealth() {
        return mValues.getAsInteger(Util.nameOf(CardColumn.HEALTH));
    }

    @Override
    public String getHowToGetThisCard() {
        return mValues.getAsString(Util.nameOf(CardColumn.HOW_TO_GET_THIS_CARD));
    }

    @Override
    public String getHowToGetThisGoldCard() {
        return mValues.getAsString(Util.nameOf(CardColumn.HOW_TO_GET_THIS_GOLD_CARD));
    }

    @Override
    public String getImageUri() {
        return "images/cards/card-" + getCardId() + ".png";
    }

    @Override
    public Race getRace() {
        return getEnum(Race.class, CardColumn.RACE);
    }

    @Override
    public Rarity getRarity() {
        return getEnum(Rarity.class, CardColumn.RARITY);
    }

    @Override
    public boolean isSecret() {
        return mValues.getAsInteger(Util.nameOf(CardColumn.SECRET)) != 0;
    }

    private <E extends Enum<E>> E getEnum(java.lang.Class<E> cls, CardColumn column) {
        return Util.toEnum(cls, mValues.getAsString(Util.nameOf(column)));
    }

}
