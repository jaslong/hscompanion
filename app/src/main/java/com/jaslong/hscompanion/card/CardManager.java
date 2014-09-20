package com.jaslong.hscompanion.card;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.text.TextUtils;

import com.jaslong.hscompanion.database.CardColumn;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.HeroClass;
import com.jaslong.util.android.app.CachedAsyncTaskLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardManager extends CachedAsyncTaskLoader<List<Card>> {

    private static final Uri CARDS_URI =
            Uri.parse("content://com.jaslong.hscompanion.contentprovider/cards");
    private static final Uri CARDS_QUERY_URI =
            Uri.parse("content://com.jaslong.hscompanion.contentprovider/cards/query");

    private static final String DEFAULT_SORT_ORDER = TextUtils.join(",", new Object[]{
            "CASE WHEN %1$s." + CardColumn.CLASS.getDescription().getName()
                    + " IS NULL THEN 1 ELSE 0 END",
            "%1$s." + CardColumn.CLASS.getDescription().getName(),
            "%1$s." + CardColumn.MANA_COST.getDescription().getName(),
            "%1$s." + CardColumn.NAME.getDescription().getName()
    });

    public static final String QUERY_SECRETS = "!secrets!";
    public static final String QUERY_CARD = "!card!";

    private final HeroClass mHeroClass;
    private final ContentResolver mContentResolver;
    private String mQuery;

    public CardManager(Context context) {
        this(context, null);
    }

    public CardManager(Context context, HeroClass heroClass) {
        super(context);
        mHeroClass = heroClass;
        mContentResolver = context.getContentResolver();
    }

    public void requestQuery(String query) {
        if (!TextUtils.equals(mQuery, query)) {
            mQuery = query;
            onContentChanged();
        }
    }

    public void requestCard(String cardId) {
        requestQuery(QUERY_CARD + cardId);
    }

    @Override
    public List<Card> loadInBackground() {
        if (TextUtils.isEmpty(mQuery)) {
            String selection = null;
            String[] selectionArgs = null;
            if (mHeroClass != null) {
                selection = Util.nameOf(CardColumn.CLASS) + "=? OR "
                        + Util.nameOf(CardColumn.CLASS) + " IS NULL";
                selectionArgs = new String[] { mHeroClass.toString() };
            }
            return getCardList(selection, selectionArgs);
        } else if (QUERY_SECRETS.equals(mQuery)) {
            return getSecrets();
        } else if (mQuery.startsWith(QUERY_CARD)) {
            return getCard(mQuery.substring(QUERY_CARD.length()));
        } else {
            Uri.Builder uriBuilder = CARDS_QUERY_URI.buildUpon();
            uriBuilder.appendPath(mQuery);
            if (mHeroClass != null) {
                uriBuilder.appendPath(mHeroClass.toString());
            }
            Cursor cursor = mContentResolver.query(
                    uriBuilder.build(),
                    null,
                    null,
                    null,
                    DEFAULT_SORT_ORDER);
            List<Card> cardList = toCardList(cursor);
            cursor.close();
            return cardList;

        }
    }

    public List<Card> getCard(String cardId) {
        Cursor cursor = mContentResolver.query(
                Uri.withAppendedPath(CARDS_URI, cardId),
                null,
                null,
                null,
                null);
        Card card = cursor.moveToNext() ? toCard(cursor) : null;
        cursor.close();
        return Collections.singletonList(card);
    }

    private List<Card> getSecrets() {
        return getCardList(CardColumn.SECRET.getDescription().getName() + "=1", null);
    }

    private List<Card> getCardList(String selection, String[] selectionArgs) {
        Cursor cursor = mContentResolver.query(
                CARDS_URI,
                null,
                selection,
                selectionArgs,
                DEFAULT_SORT_ORDER);
        List<Card> cardList = toCardList(cursor);
        cursor.close();
        return cardList;
    }

    private static List<Card> toCardList(Cursor cursor) {
        List<Card> cardList = new ArrayList<>(cursor.getCount());
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            cardList.add(toCard(cursor));
        }
        return Collections.unmodifiableList(cardList);
    }

    private static Card toCard(Cursor cursor) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        return toCard(values);
    }

    private static Card toCard(ContentValues values) {
        return new ContentValuesCard(values);
    }

}
