package com.jaslong.hscompanion.util;

public final class Logger extends com.jaslong.util.android.log.Logger {

    private static final String TAG = "jHS";

    public static Logger create(String... categories) {
        return new Logger(categories);
    }

    private Logger(String... categories) {
        super(TAG, categories);
    }

}
