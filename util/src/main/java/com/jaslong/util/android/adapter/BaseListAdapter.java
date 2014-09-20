package com.jaslong.util.android.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.Collections;
import java.util.List;

public abstract class BaseListAdapter<T> extends BaseAdapter {

    protected final Context mContext;
    private final int mLayoutResId;
    private List<T> mList;

    protected BaseListAdapter(Context context, @LayoutRes int layoutResId) {
        mContext = context;
        mLayoutResId = layoutResId;
        mList = Collections.emptyList();
    }

    protected abstract void bindView(View view, Context context, T item, int position);

    public void swapList(List<T> list) {
        mList = list != null ? list : Collections.<T>emptyList();
        notifyDataSetChanged();
    }

    public T getTypedItem(int position) {
        return position < mList.size() ? mList.get(position) : null;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return getTypedItem(position);
    }

    @Override
    public long getItemId(int position) {
        return 0L;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(mLayoutResId, parent, false);
        }
        bindView(view, mContext, mList.get(position), position);
        return view;
    }

}
