package com.jaslong.util.android.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.MultiAutoCompleteTextView;

/**
 * {@link MultiAutoCompleteTextView} that always shows completions.
 */
public class DefaultAutoCompleteTextView extends MultiAutoCompleteTextView {

    public interface OnImeBackListener {
        void onImeBack(View view);
    }

    private ListAdapter mDefaultAdapter;
    private ListAdapter mActualAdapter;
    private OnImeBackListener mOnImeBackListener;

    public DefaultAutoCompleteTextView(Context context) {
        super(context);
        init();
    }

    public DefaultAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DefaultAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setThreshold(1);
    }

    public void setOnImeBackListener(OnImeBackListener listener) {
        mOnImeBackListener = listener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, @NonNull KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && mOnImeBackListener != null) {
            mOnImeBackListener.onImeBack(this);
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public boolean enoughToFilter() {
        CharSequence text = getText();
        return TextUtils.getTrimmedLength(text) == 0 ||
                (super.enoughToFilter() && meetsThreshold(text));
    }

    public <T extends ListAdapter & Filterable> void setDefaultAdapter(T adapter) {
        mDefaultAdapter = adapter;
        if (!meetsThreshold(getText())) {
            super.setAdapter(adapter);
        }
    }

    @Override
    public <T extends ListAdapter & Filterable> void setAdapter(@NonNull T adapter) {
        mActualAdapter = adapter;
        if (meetsThreshold(getText())) {
            super.setAdapter(adapter);
        }
    }

    @Override
    protected void performFiltering(@NonNull CharSequence text, int keyCode) {
        ListAdapter currentAdapter = getAdapter();
        if (meetsThreshold(text)) {
            if (currentAdapter != mActualAdapter) {
                super.setAdapter(cast(mActualAdapter));
            }
            super.performFiltering(text, keyCode);
        } else {
            if (currentAdapter != mDefaultAdapter) {
                super.setAdapter(cast(mDefaultAdapter));
            }
            super.performFiltering(text, keyCode);
        }
    }

    private boolean meetsThreshold(CharSequence text) {
        return TextUtils.getTrimmedLength(text) >= getThreshold();
    }

    @SuppressWarnings("unchecked")
    private static <T extends ListAdapter & Filterable> T cast(ListAdapter adapter) {
        if (adapter instanceof Filterable) {
            return (T) adapter;
        } else {
            throw new IllegalArgumentException();
        }
    }

}
