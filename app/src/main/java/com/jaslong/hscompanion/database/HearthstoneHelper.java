package com.jaslong.hscompanion.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jaslong.hscompanion.expansion.ExpansionUtil;
import com.jaslong.hscompanion.util.Logger;
import com.jaslong.hscompanion.search.SearchUtils;
import com.jaslong.hscompanion.card.reader.CardReader;
import com.jaslong.hscompanion.card.reader.json.JsonCardReader;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.util.android.database.SQLiteTable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class HearthstoneHelper extends SQLiteOpenHelper {

    private static final Logger sLogger = Logger.create("DB", "HearthstoneHelper");

    private static final String DATABASE_NAME = "com.jaslong.hscompanion.database";
    private static final int VERSION = 2;
    private static final List<SQLiteTable> STATIC_TABLES = Arrays.asList(
            HearthstoneTables.CARD,
            HearthstoneTables.WORD_CARD);
    private static final List<SQLiteTable> USER_TABLES = Arrays.asList(
            HearthstoneTables.DECK,
            HearthstoneTables.DECK_CARD);

    private static final String INITIAL_CARDS_JSON_FILE = "cards.json";

    public HearthstoneHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (SQLiteTable table : STATIC_TABLES) {
            sLogger.dc("Creating static table " + table.getName());
            db.execSQL(table.getCreate());
        }
        for (SQLiteTable table : USER_TABLES) {
            sLogger.dc("Creating user table " + table.getName());
            db.execSQL(table.getCreate());
        }
        initializeTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        sLogger.i(String.format("Upgrading from %d to %d", oldVersion, newVersion));
        for (int i = STATIC_TABLES.size() - 1; i >= 0; --i) {
            SQLiteTable table = STATIC_TABLES.get(i);
            sLogger.dc("Dropping static table " + table.getName());
            db.execSQL(table.getDrop());
        }
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private boolean initializeTables(final SQLiteDatabase db) {
        CardReader reader = new JsonCardReader();
        db.beginTransaction();
        try {
            InputStream inputStream =
                    ExpansionUtil.getExpansionFile().getInputStream(INITIAL_CARDS_JSON_FILE);
            reader.read(inputStream, new CardReader.Callback() {
                @Override
                public void onCardRead(Card card) {
                    if (card.isCollectible() && card.getType() != null) {
                        try {
                            addToCardTable(db, card);
                            addToWordCardTable(db, card);
                        } catch (Exception e) {
                            sLogger.e("Card failed: " + card.getName());
                            throw e;
                        }
                    } else {
                        sLogger.dc("Not adding un-collectible card: " + card.getName());
                    }
                }
            });
            db.setTransactionSuccessful();
            return true;
        } catch (IOException e) {
            sLogger.w(e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    private void addToCardTable(SQLiteDatabase db, Card card) {
        long result = db.insert(
                HearthstoneTables.CARD.getName(), null, toContentValues(card));
        if (result != -1L) {
            sLogger.dc("Added card: " + card.getName());
        } else {
            sLogger.dc("Failed to add card: " + card.getName());
        }
    }

    private void addToWordCardTable(SQLiteDatabase db, Card card) {
        HashSet<String> words = new HashSet<>();
        // Name and all substrings of name.
        words.addAll(SearchUtils.tokenizeAndGetAllSubstrings(card.getName()));
        // All keywords in the card text.
        words.addAll(SearchUtils.findKeywords(card.getText()));

        words.add(String.valueOf(card.getManaCost()));
        if (card.getHeroClass() != null) {
            words.addAll(SearchUtils.findKeywords(card.getHeroClass().toString()));
        }
        words.addAll(SearchUtils.findKeywords(card.getSet().toString()));
        words.addAll(SearchUtils.findKeywords(card.getType().toString()));
        if (card.getRace() != null) {
            words.addAll(SearchUtils.findKeywords(card.getRace().toString()));
        }
        words.addAll(SearchUtils.findKeywords(card.getRarity().toString()));
        sLogger.dc("Card " + card.getName() + " has words: " + words);
        for (String word : words) {
            ContentValues values = new ContentValues(2);
            values.put(WordCardColumn.WORD.toString(), word);
            values.put(WordCardColumn.CARD_ID.toString(), card.getCardId());
            db.insert(HearthstoneTables.WORD_CARD.getName(), null, values);
        }
    }

    private static ContentValues toContentValues(Card card) {
        ContentValues values = new ContentValues(CardColumn.values().length);
        values.put(CardColumn.ARTIST_NAME.getDescription().getName(), card.getArtistName());
        values.put(CardColumn.ATTACK.getDescription().getName(), card.getAttack());
        if (card.getHeroClass() != null) {
            values.put(CardColumn.CLASS.getDescription().getName(), card.getHeroClass().toString());
        }
        values.put(CardColumn.CARD_ID.getDescription().getName(), card.getCardId());
        values.put(CardColumn.NAME.getDescription().getName(), card.getName());
        if (card.getSet() != null) {
            values.put(CardColumn.SET.getDescription().getName(), card.getSet().toString());
        }
        values.put(CardColumn.TEXT.getDescription().getName(),
                card.getText());
        if (card.getType() != null) {
            values.put(CardColumn.TYPE.getDescription().getName(), card.getType().toString());
        }
        values.put(CardColumn.COLLECTIBLE.getDescription().getName(), card.isCollectible());
        values.put(CardColumn.MANA_COST.getDescription().getName(), card.getManaCost());
        values.put(CardColumn.DURABILITY.getDescription().getName(), card.getDurability());
        values.put(CardColumn.FLAVOR_TEXT.getDescription().getName(), card.getFlavorText());
        values.put(CardColumn.HEALTH.getDescription().getName(), card.getAttack());
        values.put(CardColumn.HOW_TO_GET_THIS_CARD.getDescription().getName(),
                card.getHowToGetThisCard());
        values.put(CardColumn.HOW_TO_GET_THIS_GOLD_CARD.getDescription().getName(),
                card.getHowToGetThisGoldCard());
        if (card.getRace() != null) {
            values.put(CardColumn.RACE.getDescription().getName(), card.getRace().toString());
        }
        if (card.getRarity() != null) {
            values.put(CardColumn.RARITY.getDescription().getName(), card.getRarity().toString());
        }
        values.put(CardColumn.SECRET.getDescription().getName(), card.isSecret());
        return values;
    }

}
