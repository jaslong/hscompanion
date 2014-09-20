package com.jaslong.util.android.concurrent;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * An {@link java.util.concurrent.Executor} that wraps a {@link android.os.Handler}.
 */
public class HandlerExecutor implements Executor {

    private final Handler mHandler;


    /**
     * Creates an {@link java.util.concurrent.Executor} for the current thread.
     */
    public static HandlerExecutor forCurrentThread() {
        return new HandlerExecutor(new Handler());
    }

    /**
     * Creates an {@link java.util.concurrent.Executor} for the main thread.
     */
    public static HandlerExecutor forMainThread() {
        return new HandlerExecutor(new Handler(Looper.getMainLooper()));
    }

    /**
     * Creates an {@link java.util.concurrent.Executor} that uses the specified handler.
     * @param handler handler to use
     */
    public static HandlerExecutor forThread(Handler handler) {
        return new HandlerExecutor(handler);
    }

    private HandlerExecutor(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void execute(Runnable command) {
        mHandler.post(command);
    }

}
