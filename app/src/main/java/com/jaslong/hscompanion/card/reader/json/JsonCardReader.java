package com.jaslong.hscompanion.card.reader.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.jaslong.hscompanion.card.reader.CardReader;
import com.jaslong.hscompanion.card.reader.Metadata;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.HeroClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Reads cards from a JSON file. This class is not thread-safe.
 *
 * VERSION 1
 * <cardset>
 * {
 *   "version": <int>
 *   "timestamp": <long>
 *   "cards": [
 *     *<card>
 *   ]
 * }
 */
public class JsonCardReader implements CardReader {

    private final Gson mGson;
    private Integer mVersion;
    private Long mTimestamp;

    public JsonCardReader() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        LenientEnumAdapter.register(gsonBuilder, HeroClass.class);
        LenientEnumAdapter.register(gsonBuilder, Card.Race.class);
        LenientEnumAdapter.register(gsonBuilder, Card.Rarity.class);
        LenientEnumAdapter.register(gsonBuilder, Card.Set.class);
        LenientEnumAdapter.register(gsonBuilder, Card.Type.class);
        mGson = gsonBuilder.create();
    }

    @Override
    public Metadata read(InputStream inputStream, Callback callback) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "version":
                    mVersion = reader.nextInt();
                    break;
                case "timestamp":
                    mTimestamp = reader.nextLong();
                    break;
                case "cards":
                    readCards(reader, callback);
                    break;
            }
        }
        reader.endObject();
        if (mVersion != null && mTimestamp != null) {
            return new Metadata(mVersion, mTimestamp);
        } else {
            throw new IllegalStateException("Could not find version or timestamp.");
        }
    }

    private void readCards(JsonReader reader, Callback callback) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            readCard(reader, callback);
        }
        reader.endArray();
    }

    private void readCard(JsonReader reader, Callback callback) throws IOException {
        Card card = mGson.fromJson(reader, JsonCard.class);
        callback.onCardRead(card);
    }

}
