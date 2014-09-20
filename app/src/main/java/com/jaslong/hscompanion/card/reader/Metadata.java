package com.jaslong.hscompanion.card.reader;

/**
 * Metadata for a reading of cards.
 */
public final class Metadata {

    private final int mVersion;
    private final long mTimestamp;

    public Metadata(int version, long timestamp) {
        mVersion = version;
        mTimestamp = timestamp;
    }

    public int getVersion() {
        return mVersion;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

}
