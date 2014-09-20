package com.jaslong.util.android.database;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public interface Column {

    public enum Type {
        INTEGER, TEXT
    }

    public enum Modifier {
        PRIMARY_KEY, UNIQUE, NOT_NULL
    }

    public final class Description {

        private final String mName;
        private final Type mType;
        private final Set<Modifier> mModifiers;

        public Description(String name, Type type, Modifier... modifiers) {
            mName = name;
            mType = type;
            mModifiers = modifiers.length > 0 ? EnumSet.copyOf(Arrays.asList(modifiers)) :
                    Collections.<Modifier>emptySet();
        }

        public String getName() {
            return mName;
        }

        public Type getType() {
            return mType;
        }

        public Set<Modifier> getModifiers() {
            return mModifiers;
        }

        public boolean is(Modifier modifier) {
            return mModifiers.contains(modifier);
        }

    }

    Description getDescription();
}
