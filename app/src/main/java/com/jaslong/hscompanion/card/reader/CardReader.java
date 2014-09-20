package com.jaslong.hscompanion.card.reader;

import com.jaslong.hscompanion.model.Card;

import java.io.IOException;
import java.io.InputStream;

public interface CardReader {

    public interface Callback {
        void onCardRead(Card card);
    }

    Metadata read(InputStream inputStream, Callback callback) throws IOException;

}
