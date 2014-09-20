package com.jaslong.hscompanion.card;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.text.TextUtils;

import com.jaslong.hscompanion.util.Logger;
import com.jaslong.hscompanion.contentprovider.HearthstoneContentProvider;
import com.jaslong.hscompanion.database.DeckCardColumn;
import com.jaslong.hscompanion.database.DeckColumn;
import com.jaslong.hscompanion.model.Deck;
import com.jaslong.hscompanion.model.DeckCard;
import com.jaslong.hscompanion.model.HeroClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeckManager {

    private static final Logger sLogger = Logger.create("DB", "DeckManager");

    public static final Uri DECKS_URI =
            Uri.parse("content://com.jaslong.hscompanion.contentprovider/decks");
    public static final Uri DECK_CARD_URI =
            Uri.parse("content://com.jaslong.hscompanion.contentprovider/deck_card");

    private static final String DEFAULT_SORT_ORDER = TextUtils.join(",", new Object[]{
            DeckColumn.HERO_CLASS.getDescription().getName(),
            DeckColumn.NAME.getDescription().getName()
    });

    private final ContentResolver mContentResolver;

    public DeckManager(Context context) {
        mContentResolver = context.getContentResolver();
    }

    public List<Deck> getDecks() {
        Cursor cursor = mContentResolver.query(
                DECKS_URI,
                null,
                null,
                null,
                DEFAULT_SORT_ORDER);

        List<Deck> deckList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            ContentValues contentValues = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
            deckList.add(new ContentValuesDeck(contentValues));
        }
        cursor.close();
        return Collections.unmodifiableList(deckList);
    }

    public Deck getDeck(long deckId) {
        Cursor cursor = mContentResolver.query(
                createDeckUri(deckId),
                null,
                null,
                null,
                DEFAULT_SORT_ORDER);

        Deck deck;
        if (cursor.moveToNext()) {
            ContentValues contentValues = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
            deck = new ContentValuesDeck(contentValues);
            cursor.close();
            return deck;
        } else {
            return null;
        }
    }

    public Long addDeck(final String name, final HeroClass heroClass) {
        ContentValues values = new ContentValues(2);
        values.put(Util.nameOf(DeckColumn.NAME), name);
        values.put(Util.nameOf(DeckColumn.HERO_CLASS), heroClass.toString());
        Uri uri = mContentResolver.insert(DECKS_URI, values);
        if (uri != null) {
            mContentResolver.notifyChange(uri, null);
            return Long.valueOf(uri.getLastPathSegment());
        } else {
            return null;
        }
    }

    public boolean renameDeck(final long deckId, final String name) {
        Uri uri = createDeckUri(deckId);
        ContentValues values = new ContentValues(1);
        values.put(Util.nameOf(DeckColumn.NAME), name);
        boolean success = mContentResolver.update(
                uri,
                values,
                null,
                null) > 0;
        if (success) {
            mContentResolver.notifyChange(uri, null);
        }
        return success;
    }

    public boolean deleteDeck(long deckId) {
        Uri deckUri = createDeckUri(deckId);
        mContentResolver.delete(createDeckCardUri(deckId), null, null);
        boolean success = mContentResolver.delete(
                deckUri,
                null,
                null) > 0;
        if (success) {
            mContentResolver.notifyChange(deckUri, null);
        }
        return success;
    }

    public List<DeckCard> getDeckCards(long deckId) {
        Cursor cursor = mContentResolver.query(
                createDeckCardUri(deckId),
                null,
                null,
                null,
                null);
        List<DeckCard> deckCardList = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            ContentValues values = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, values);
            DeckCard deckCard = new ContentValuesDeckCard(values);
            deckCardList.add(deckCard);
        }
        cursor.close();
        return deckCardList;
    }

    public boolean updateDeckCard(DeckCard deckCard) {
        ContentValues values = new ContentValues(3);
        values.put(Util.nameOf(DeckCardColumn.DECK_ID), deckCard.getDeckId());
        values.put(Util.nameOf(DeckCardColumn.CARD_ID), deckCard.getCard().getCardId());
        values.put(Util.nameOf(DeckCardColumn.COUNT), deckCard.getCount());
        values.put(HearthstoneContentProvider.INSERT_OR_REPLACE, true);
        Uri uri = mContentResolver.insert(DECK_CARD_URI, values);
        if (uri != null) {
            mContentResolver.notifyChange(createDeckCardUri(deckCard.getDeckId()), null);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeDeckCard(DeckCard deckCard) {
        int deletedRows = mContentResolver.delete(DECK_CARD_URI,
                Util.nameOf(DeckCardColumn.DECK_ID) + "=? AND "
                        + Util.nameOf(DeckCardColumn.CARD_ID) + "=?",
                new String[] {
                        String.valueOf(deckCard.getDeckId()),
                        deckCard.getCard().getCardId() });
        if (deletedRows == 0) {
            return false;
        } else if (deletedRows == 1) {
            mContentResolver.notifyChange(createDeckCardUri(deckCard.getDeckId()), null);
            return true;
        } else {
            sLogger.w("More than one deck card deleted!");
            mContentResolver.notifyChange(createDeckCardUri(deckCard.getDeckId()), null);
            return true;
        }
    }

    private static Uri createDeckUri(long deckId) {
        return Uri.withAppendedPath(DECKS_URI, String.valueOf(deckId));
    }

    private static Uri createDeckCardUri(long deckId) {
        return Uri.withAppendedPath(DECK_CARD_URI, String.valueOf(deckId));
    }

}
