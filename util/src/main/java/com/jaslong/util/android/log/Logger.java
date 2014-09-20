package com.jaslong.util.android.log;

import android.util.Log;

public class Logger {

    private static final int ASSERT = Log.ASSERT;
    private static final int VERBOSE = Log.VERBOSE;
    private static final int DEBUG = Log.DEBUG;
    private static final int INFO = Log.INFO;
    private static final int WARN = Log.WARN;
    private static final int ERROR = Log.ERROR;

    private final String mTag;
    private final String mPrefix;

    public Logger(String tag, String... categories) {
        mTag = tag;
        StringBuilder s = new StringBuilder();
        String sep = "";
        s.append('[');
        for (String category : categories) {
            s.append(sep).append(category);
            sep = ",";
        }
        s.append("] ");
        mPrefix = s.toString();
    }

    public boolean isLoggable(int level) {
        return Log.isLoggable(mTag, level);
    }

    public void v(String msg) {
        Log.v(mTag, format(msg));
    }

    public void v(String msg, Throwable tr) {
        Log.v(mTag, format(msg), tr);
    }

    public void vc(String msg) {
        if (isLoggable(VERBOSE)) v(msg);
    }

    public void vc(String msg, Throwable tr) {
        if (isLoggable(VERBOSE)) v(msg, tr);
    }

    public void d(String msg) {
        Log.d(mTag, format(msg));
    }

    public void d(String msg, Throwable tr) {
        Log.d(mTag, format(msg), tr);
    }

    public void dc(String msg) {
        if (isLoggable(DEBUG)) d(msg);
    }

    public void dc(String msg, Throwable tr) {
        if (isLoggable(DEBUG)) d(msg, tr);
    }

    public void i(String msg) {
        Log.i(mTag, format(msg));
    }

    public void i(String msg, Throwable tr) {
        Log.i(mTag, format(msg), tr);
    }

    public void ic(String msg) {
        if (isLoggable(INFO)) i(msg);
    }

    public void ic(String msg, Throwable tr) {
        if (isLoggable(INFO)) i(msg, tr);
    }

    public void w(String msg) {
        Log.w(mTag, format(msg));
    }

    public void w(String msg, Throwable tr) {
        Log.w(mTag, format(msg), tr);
    }

    public void w(Throwable tr) {
        Log.w(mTag, tr);
    }

    public void wc(String msg) {
        if (isLoggable(WARN)) w(msg);
    }

    public void wc(String msg, Throwable tr) {
        if (isLoggable(WARN)) w(msg, tr);
    }

    public void wc(Throwable tr) {
        if (isLoggable(WARN)) w(tr);
    }

    public void e(String msg) {
        Log.e(mTag, format(msg));
    }

    public void e(String msg, Throwable tr) {
        Log.e(mTag, format(msg), tr);
    }

    public void ec(String msg) {
        if (isLoggable(ERROR)) e(msg);
    }

    public void ec(String msg, Throwable tr) {
        if (isLoggable(ERROR)) e(msg, tr);
    }

    public void wtf(String msg) {
        Log.wtf(mTag, format(msg));
    }

    public void wtf(String msg, Throwable tr) {
        Log.wtf(mTag, format(msg), tr);
    }

    public void wtf(Throwable tr) {
        Log.wtf(mTag, tr);
    }

    public void wtfc(String msg) {
        if (isLoggable(ASSERT)) wtf(msg);
    }

    public void wtfc(String msg, Throwable tr) {
        if (isLoggable(ASSERT)) wtf(msg, tr);
    }

    public void wtfc(Throwable tr) {
        if (isLoggable(ASSERT)) wtf(tr);
    }

    private String format(String msg) {
        return mPrefix + msg;
    }

}
