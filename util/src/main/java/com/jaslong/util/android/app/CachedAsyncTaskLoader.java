package com.jaslong.util.android.app;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * AsyncTaskLoader with more methods implemented.
 */
public abstract class CachedAsyncTaskLoader<D> extends AsyncTaskLoader<D> {

    private D mLatestResult;

    protected CachedAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mLatestResult != null) {
            deliverResult(mLatestResult);
        }

        if (takeContentChanged() || mLatestResult == null) {
            forceLoad();
        }
    }

    @Override
    protected D onLoadInBackground() {
        mLatestResult = super.onLoadInBackground();
        return mLatestResult;
    }

}
