package com.jaslong.util.android.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Custom fragment for utility.
 */
public class RetainedFragment extends Fragment {

    public interface ActivityRunnable {
        void run(Activity activity);
    }

    private final BlockingQueue<ActivityRunnable> mQueue;
    private final Object mActivityLock = new Object();
    private Activity mActivity;

    public RetainedFragment() {
        mQueue = new LinkedBlockingQueue<ActivityRunnable>();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        synchronized (mActivityLock) {
            mActivity = activity;
            while (!mQueue.isEmpty()) {
                mQueue.poll().run(mActivity);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        synchronized (mActivityLock) {
            mActivity = null;
        }
    }

    protected void post(ActivityRunnable runnable) {
        boolean ran = false;
        synchronized (mActivity) {
            if (mActivity != null) {
                runnable.run(mActivity);
                ran = true;
            }
        }
        if (!ran) {
            mQueue.add(runnable);
        }
    }

}
