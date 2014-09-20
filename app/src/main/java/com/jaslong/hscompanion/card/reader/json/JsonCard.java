package com.jaslong.hscompanion.card.reader.json;

import com.google.gson.annotations.SerializedName;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.HeroClass;

public class JsonCard implements Card {

    @SerializedName("ArtistName")
    private String mArtistName;

    @SerializedName("Attack")
    private Integer mAttack;

    @SerializedName("Class")
    private HeroClass mClass;

    @SerializedName("CardID")
    private String mCardId;

    @SerializedName("Name")
    private String mName;

    @SerializedName("Set")
    private Set mSet;

    @SerializedName("Text")
    private String mText;

    @SerializedName("Type")
    private Type mType;

    @SerializedName("Collectible")
    private int mCollectible;

    @SerializedName("ManaCost")
    private int mManaCost;

    @SerializedName("Durability")
    private Integer mDurability;

    @SerializedName("FlavorText")
    private String mFlavorText;

    @SerializedName("Health")
    private Integer mHealth;

    @SerializedName("HowToGetThisCard")
    private String mHowToGetThisCard;

    @SerializedName("HowToGetThisGoldCard")
    private String mHowToGetThisGoldCard;

    @SerializedName("ImageUri")
    private String mImageUri;

    @SerializedName("Race")
    private Race mRace;

    @SerializedName("Rarity")
    private Rarity mRarity;

    @SerializedName("Secret")
    private int mSecret;

    @Override
    public String getArtistName() {
        return mArtistName;
    }

    @Override
    public Integer getAttack() {
        return mAttack;
    }

    @Override
    public HeroClass getHeroClass() {
        return mClass;
    }

    @Override
    public String getCardId() {
        return mCardId;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Set getSet() {
        return mSet;
    }

    @Override
    public String getText() {
        return mText;
    }

    @Override
    public Type getType() {
        return mType;
    }

    @Override
    public boolean isCollectible() {
        return mCollectible != 0;
    }

    @Override
    public int getManaCost() {
        return mManaCost;
    }

    @Override
    public Integer getDurability() {
        return mDurability;
    }

    @Override
    public String getFlavorText() {
        return mFlavorText;
    }

    @Override
    public Integer getHealth() {
        return mHealth;
    }

    @Override
    public String getHowToGetThisCard() {
        return mHowToGetThisCard;
    }

    @Override
    public String getHowToGetThisGoldCard() {
        return mHowToGetThisGoldCard;
    }

    @Override
    public String getImageUri() {
        return mImageUri;
    }

    @Override
    public Race getRace() {
        return mRace;
    }

    @Override
    public Rarity getRarity() {
        return mRarity;
    }

    @Override
    public boolean isSecret() { return mSecret != 0; }

}
