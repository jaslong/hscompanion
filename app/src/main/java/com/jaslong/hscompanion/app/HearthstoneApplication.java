package com.jaslong.hscompanion.app;

import android.app.Application;
import android.os.Debug;

import com.jaslong.hscompanion.BuildConfig;
import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.util.Logger;
import com.jaslong.util.android.view.ViewHolder;

import java.io.IOException;

public class HearthstoneApplication extends Application {

    private static Logger sLogger = Logger.create("HearthstoneApplication");

    private static HearthstoneApplication mApp;

    public static HearthstoneApplication getInstance() {
        if (mApp == null) {
            throw new IllegalStateException("mApp is null!");
        }
        return mApp;
    }

    public HearthstoneApplication() {
        synchronized (HearthstoneApplication.class) {
            if (mApp != null) {
                throw new IllegalStateException("mApp already set!");
            }
            mApp = this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler());

        ViewHolder.setKey(R.id.view_holder_key);

    }

    private class UncaughtExceptionHandler
            implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            sLogger.e("Crashed with uncaught exception!", ex);
            if (BuildConfig.DEBUG && ex instanceof OutOfMemoryError) {
                try {
                    String location = getFilesDir().getAbsolutePath() + "/dump.hprof";
                    sLogger.i("Dumping hprof to " + location);
                    Debug.dumpHprofData(location);
                    sLogger.i("Dumped hprof to " + location);
                } catch (IOException e) {
                    sLogger.e("Couldn't dump hprof!");
                }
            }

            thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, ex);
        }
    }

}
