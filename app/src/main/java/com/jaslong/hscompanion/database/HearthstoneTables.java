package com.jaslong.hscompanion.database;

import com.jaslong.util.android.database.SQLiteTable;

public final class HearthstoneTables {

    public static SQLiteTable CARD = new SQLiteTable("card", CardColumn.values());
    public static SQLiteTable DECK = new SQLiteTable("deck", DeckColumn.values());
    public static SQLiteTable DECK_CARD = new SQLiteTable("deck_card", DeckCardColumn.values(),
            DeckCardColumn.DECK_ID, DeckCardColumn.CARD_ID);
    public static SQLiteTable WORD_CARD = new SQLiteTable("word_card", WordCardColumn.values(),
            WordCardColumn.WORD, WordCardColumn.CARD_ID);

    private HearthstoneTables() {
        throw new UnsupportedOperationException();
    }

}
