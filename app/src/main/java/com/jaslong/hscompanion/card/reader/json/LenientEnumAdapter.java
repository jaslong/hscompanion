package com.jaslong.hscompanion.card.reader.json;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jaslong.hscompanion.card.Util;

import java.io.IOException;

/**
 * Adapter for enums that is lenient, converting the serialized form to uppercase.
 *
 * Example: "Druid" matches DRUID.
 */
public class LenientEnumAdapter<E extends Enum<E>> extends TypeAdapter<E> {

    private final Class<E> mCls;

    public static <E extends Enum<E>> void register(GsonBuilder gsonBuilder, Class<E> cls) {
        gsonBuilder.registerTypeAdapter(cls, new LenientEnumAdapter<>(cls));
    }

    public LenientEnumAdapter(Class<E> cls) {
        mCls = cls;
    }

    @Override
    public void write(JsonWriter out, E value) throws IOException {
        out.value(value.toString());
    }

    @Override
    public E read(JsonReader in) throws IOException {
        String name = in.nextString();
        if (name == null) {
            return null;
        }
        try {
            return Util.toEnum(mCls, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
