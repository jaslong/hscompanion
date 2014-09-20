package com.jaslong.hscompanion.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.SparseArray;

import com.jaslong.hscompanion.util.Logger;
import com.jaslong.hscompanion.card.Util;
import com.jaslong.hscompanion.database.CardColumn;
import com.jaslong.hscompanion.database.DeckCardColumn;
import com.jaslong.hscompanion.database.DeckColumn;
import com.jaslong.hscompanion.database.HearthstoneHelper;
import com.jaslong.hscompanion.database.HearthstoneTables;
import com.jaslong.hscompanion.database.WordCardColumn;
import com.jaslong.hscompanion.search.SearchUtils;
import com.jaslong.util.android.database.SQLiteTable;

import java.util.LinkedList;
import java.util.List;

public class HearthstoneContentProvider extends ContentProvider {

    private static final Logger sLogger = Logger.create("DB", "HearthstoneContentProvider");

    public static final String INSERT_OR_REPLACE = "__insert_or_replace";

    private static final String AUTHORITY = "com.jaslong.hscompanion.contentprovider";
    private static final int CARDS = 100;
    private static final int CARDS_ID = 101;
    private static final int CARDS_QUERY = 102;
    private static final int CARDS_QUERY_HERO_CLASS = 103;
    private static final int DECKS = 200;
    private static final int DECKS_ID = 201;
    private static final int DECK_CARD = 300;
    private static final int DECK_CARD_DECK_ID = 301;

    private static final UriMatcher sUriMatcher;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "cards", CARDS);
        sUriMatcher.addURI(AUTHORITY, "cards/query/*", CARDS_QUERY);
        sUriMatcher.addURI(AUTHORITY, "cards/query/*/*", CARDS_QUERY_HERO_CLASS);
        sUriMatcher.addURI(AUTHORITY, "cards/*", CARDS_ID);
        sUriMatcher.addURI(AUTHORITY, "decks", DECKS);
        sUriMatcher.addURI(AUTHORITY, "decks/#", DECKS_ID);
        sUriMatcher.addURI(AUTHORITY, "deck_card", DECK_CARD);
        sUriMatcher.addURI(AUTHORITY, "deck_card/*", DECK_CARD_DECK_ID);
    }

    private static final SparseArray<SQLiteTable> TABLES;
    static {
        TABLES = new SparseArray<>(6);
        TABLES.put(CARDS, HearthstoneTables.CARD);
        TABLES.put(CARDS_ID, HearthstoneTables.CARD);
        TABLES.put(DECKS, HearthstoneTables.DECK);
        TABLES.put(DECKS_ID, HearthstoneTables.DECK);
        TABLES.put(DECK_CARD, HearthstoneTables.DECK_CARD);
        TABLES.put(DECK_CARD_DECK_ID, HearthstoneTables.DECK_CARD);
    }

    private SQLiteOpenHelper mHelper;

    @Override
    public boolean onCreate() {
        mHelper = new HearthstoneHelper(getContext());
        return true;
    }

    private Cursor queryDeckCardsOfDeck(long deckId) {
        return mHelper.getReadableDatabase().rawQuery(String.format(
                        "SELECT * FROM %s c INNER JOIN %s dc ON dc.%s=c.%s WHERE dc.%s=?",
                        HearthstoneTables.CARD.getName(),
                        HearthstoneTables.DECK_CARD.getName(),
                        Util.nameOf(DeckCardColumn.CARD_ID),
                        Util.nameOf(CardColumn.CARD_ID),
                        Util.nameOf(DeckCardColumn.DECK_ID)),
                new String[] { String.valueOf(deckId) });
    }

    private Cursor queryCardsQuery(String heroClass, String words, String sortOrder) {
        List<String> args = new LinkedList<>();
        StringBuilder subquerySql = new StringBuilder();
        String separator = "";

        for (String word : SearchUtils.toSearchTerms(words)) {
            subquerySql.append(separator).append(String.format("SELECT %s FROM %s WHERE %s=?",
                    Util.nameOf(WordCardColumn.CARD_ID),
                    HearthstoneTables.WORD_CARD.getName(),
                    Util.nameOf(WordCardColumn.WORD)));
            args.add(word);
            separator = " INTERSECT ";
        }
        String where = "";
        if (heroClass != null) {
            where = String.format("WHERE c.%1$s=? OR c.%1$s IS NULL",
                    Util.nameOf(CardColumn.CLASS));
            args.add(heroClass);
        }
        String sql = String.format(
                "SELECT * FROM %s c INNER JOIN (%s) ci ON ci.%s=c.%s %s ORDER BY %s",
                HearthstoneTables.CARD.getName(),
                subquerySql,
                Util.nameOf(WordCardColumn.CARD_ID),
                Util.nameOf(CardColumn.CARD_ID),
                where,
                String.format(sortOrder, "c"));
        sLogger.dc("Query: " + sql);
        return mHelper.getReadableDatabase().rawQuery(sql, args.toArray(new String[args.size()]));
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case DECK_CARD_DECK_ID:
                return queryDeckCardsOfDeck(Long.valueOf(uri.getLastPathSegment()));
            case CARDS_QUERY:
                return queryCardsQuery(null, uri.getLastPathSegment(), sortOrder);
            case CARDS_QUERY_HERO_CLASS:
                List<String> pathSegments = uri.getPathSegments();
                String heroClass = pathSegments.get(pathSegments.size() - 1);
                String query = pathSegments.get(pathSegments.size() - 2);
                return queryCardsQuery(heroClass, query, sortOrder);
        }

        SQLiteTable table = TABLES.get(match);
        if (table == null) {
            return null;
        }

        WhereClause clause = getWhereClause(uri, match);

        return mHelper.getReadableDatabase().query(
                table.getName(),
                projection,
                clause != null ? clause.select : selection,
                clause != null ? clause.args : selectionArgs,
                null, // groupBy
                null, // having
                sortOrder != null ? String.format(sortOrder, table.getName()) : null); // orderBy
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteTable table = TABLES.get(sUriMatcher.match(uri));
        if (table == null) {
            return null;
        }

        // Validate content values
        if (!table.validateContentValues(contentValues)) {
            return null;
        }

        // Try inserting or replacing
        long rowId;
        if (contentValues.getAsBoolean(INSERT_OR_REPLACE) != null) {
            contentValues.remove(INSERT_OR_REPLACE);
            rowId = mHelper.getWritableDatabase().replace(table.getName(), null, contentValues);
        } else {
            rowId = mHelper.getWritableDatabase().insert(table.getName(), null, contentValues);
        }
        if (rowId == -1L) {
            return null;
        }

        // Return inserted row's path
        String id = "";
        if (HearthstoneTables.CARD.getName().equals(table.getName())) {
            id = contentValues.getAsString(CardColumn.CARD_ID.getDescription().getName());
        } else if (HearthstoneTables.DECK.getName().equals(table.getName())) {
            id = String.valueOf(rowId);
        }
        return Uri.withAppendedPath(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        SQLiteTable table = TABLES.get(match);
        if (table == null) {
            return 0;
        }

        WhereClause clause = getWhereClause(uri, match);

        return mHelper.getWritableDatabase().delete(
                table.getName(),
                clause != null ? clause.select : selection,
                clause != null ? clause.args : selectionArgs);
    }

    @Override
    public int update(
            Uri uri,
            ContentValues contentValues,
            String selection,
            String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        SQLiteTable table = TABLES.get(match);
        if (table == null) {
            return 0;
        }

        WhereClause clause = getWhereClause(uri, match);

        return mHelper.getWritableDatabase().updateWithOnConflict(
                table.getName(),
                contentValues,
                clause != null ? clause.select : selection,
                clause != null ? clause.args : selectionArgs,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    private static WhereClause getWhereClause(Uri uri, int match) {
        switch (match) {
            case CARDS_ID:
                return new WhereClause(
                        CardColumn.CARD_ID.getDescription().getName() + "=?",
                        uri.getLastPathSegment());
            case DECKS_ID:
                return new WhereClause(
                        DeckColumn.ID.getDescription().getName() + "=?",
                        uri.getLastPathSegment());
            case DECK_CARD_DECK_ID:
                return new WhereClause(
                        DeckCardColumn.DECK_ID.getDescription().getName() + "=?",
                        uri.getLastPathSegment());
            default:
                return null;
        }
    }

    private static class WhereClause {
        public String select;
        public String[] args;
        public WhereClause(String select, String... args) {
            this.select = select;
            this.args = args;
        }
    }

}
