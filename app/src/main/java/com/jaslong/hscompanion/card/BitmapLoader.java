package com.jaslong.hscompanion.card;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;

import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.jaslong.hscompanion.expansion.ExpansionUtil;
import com.jaslong.hscompanion.util.Logger;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Loads a bitmap given a URI.
 */
public class BitmapLoader {

    private static BitmapLoader sInstance;

    public static BitmapLoader getInstance() {
        if (sInstance == null) {
            sInstance = new BitmapLoader();
        }
        return sInstance;
    }

    private static final Logger sLogger = Logger.create("BitmapLoader");

    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 4;
    private static final int MAX_QUEUE_SIZE = 100;

    private final LruCache<String, Bitmap> mCache;
    private final BiMap<String, ListenableFutureTask<Bitmap>> mTasks;
    private final Object mLock;
    private final ExecutorService mExecutor;
    private final ZipResourceFile mExpansionFile;

    private BitmapLoader() {
        long maxMemory = Runtime.getRuntime().maxMemory() / 1024L;
        int cacheSize = (int) maxMemory / 8;
        mCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
        mTasks = HashBiMap.create();
        mLock = new Object();
        mExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                1L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(MAX_QUEUE_SIZE),
                new RejectedExecutionHandler() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                        ListenableFutureTask<Bitmap> rejectedTask =
                                (ListenableFutureTask<Bitmap>) e.getQueue().poll();
                        e.execute(r); // execute originally rejected

                        synchronized (mLock) {
                            String key = mTasks.inverse().remove(rejectedTask);
                            if (key != null) {
                                sLogger.dc("Removed task with key: " + key);
                            } else {
                                sLogger.ec("Not removed!");
                            }
                        }
                    }
                }) {
            @SuppressWarnings("unchecked")
            @Override
            protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
                return (RunnableFuture<T>) runnable;
            }
        };
        mExpansionFile = ExpansionUtil.getExpansionFile();
    }

    /**
     * Loads a bitmap. The callback may be executed immediately.
     */
    public void loadBitmap(
            Resources resources,
            String bitmapUri,
            int sampleSize,
            FutureCallback<Bitmap> callback,
            Executor executor) {
        String key = createKey(bitmapUri, sampleSize);
        Bitmap bitmap = mCache.get(key);
        if (bitmap != null) {
            callback.onSuccess(bitmap);
        } else {
            loadBitmapAsync(key, resources, bitmapUri, sampleSize, callback, executor);
        }
    }

    private void loadBitmapAsync(
            String key,
            Resources resources,
            String uri,
            int sampleSize,
            FutureCallback<Bitmap> callback,
            Executor executor) {
        ListenableFutureTask<Bitmap> task;
        boolean isSubmitted = true;
        synchronized (mLock) {
            task = mTasks.get(key);
            if (task == null) {
                task = ListenableFutureTask.create(new LoadCallable(resources, uri, sampleSize));
                mTasks.put(key, task);
                isSubmitted = false;
            }
        }
        if (!isSubmitted) {
            mExecutor.submit(task);
        }

        if (callback != null && executor != null) {
            Futures.addCallback(task, callback, executor);
        }
    }

    private static String createKey(String bitmapUri, int sampleSize) {
        return bitmapUri + String.valueOf(sampleSize);
    }

    private class LoadCallable implements Callable<Bitmap> {
        private final Resources mResources;
        private final String mUri;
        private final int mSampleSize;

        public LoadCallable(Resources resources, String uri, int sampleSize) {
            mResources = resources;
            mUri = uri;
            mSampleSize = sampleSize;
        }

        @Override
        public Bitmap call() throws Exception {
            InputStream inputStream = mExpansionFile.getInputStream(mUri);
            //InputStream inputStream = mResources.getAssets().open(mUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            int density = (int) mResources.getDisplayMetrics().density;
            options.inDensity = density;
            options.inTargetDensity = density;
            options.inScreenDensity = density;
            options.inSampleSize = mSampleSize;
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);

            String key = createKey(mUri, mSampleSize);
            mCache.put(key, bitmap);
            synchronized (mLock) {
                mTasks.remove(key);
                sLogger.dc("Tasks remaining: " + mTasks.size());
            }
            return bitmap;
        }
    }

}
