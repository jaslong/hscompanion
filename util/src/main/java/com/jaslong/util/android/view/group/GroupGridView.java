package com.jaslong.util.android.view.group;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.jaslong.util.Objects;
import com.jaslong.util.android.log.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GridView with groupings.
 */
public class GroupGridView extends GroupListView {

    public interface OnItemClickListener {
        void onItemClick(GroupGridView parent, View view, int position);
    }

    private static final Logger LOG = new Logger("jUtil", "GroupListView");

    private GroupedListAdapter mAdapter;
    private OnItemClickListener mListener;
    private int mNumColumns = 3;
    private int mHorizontalSpacing = 0;
    private int mVerticalSpacing = 0;

    public GroupGridView(Context context) {
        this(context, null);
    }

    public GroupGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GroupGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        mListView.setOnTouchListener(l);
    }

    @Override
    public int getFirstVisiblePosition() {
        return mAdapter.getRealPositionFromPosition(super.getFirstVisiblePosition());
    }

    @Override
    public void setSelection(int position) {
        mAdapter.setSelection(position);
    }

    @Override
    public void setSelectionFromTop(int position, int y) {
        setSelection(position);
    }

    public int getNumColumns() {
        return mNumColumns;
    }

    public void setNumColumns(int columns) {
        if (mNumColumns != columns) {
            mNumColumns = columns;
            if (mAdapter != null) {
                mAdapter.update();
            }
            mListView.invalidateViews();
        }
    }

    public int getHorizontalSpacing() {
        return mHorizontalSpacing;
    }

    public void setHorizontalSpacing(int spacing) {
        if (mHorizontalSpacing != spacing) {
            mHorizontalSpacing = spacing;
            invalidateViews();
        }
    }

    public int getVerticalSpacing() {
        return mVerticalSpacing;
    }

    public void setVerticalSpacing(int spacing) {
        if (mVerticalSpacing != spacing) {
            mVerticalSpacing = spacing;
            invalidateViews();
        }
    }

    @Override
    protected GroupListAdapter provideAdapter(GroupListAdapter adapter) {
        mAdapter = new GroupedListAdapter(adapter);
        return mAdapter;
    }

    private static class Row {
        public final Object group;
        public final int start;
        public final int end;
        public Row(Object group, int start, int end) {
            this.group = group;
            this.start = start;
            this.end = end;
        }
    }

    private class GroupedListAdapter extends BaseAdapter
            implements GroupListAdapter, OnClickListener {

        private final GroupListAdapter mAdapter;
        private final Object mRowsLock = new Object();
        private List<Row> mRows;

        private int mRealPositionOnUpdate = -1;

        public GroupedListAdapter(GroupListAdapter adapter) {
            mAdapter = adapter;
            mRows = Collections.emptyList();

            mAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    update();
                }
                @Override
                public void onInvalidated() {
                    synchronized (mRowsLock) {
                        mRows = Collections.emptyList();
                    }
                }
            });
            update();
        }

        public void update() {
            LOG.i("Updating rows.");
            synchronized (mRowsLock) {
                mRows = new ArrayList<Row>(mAdapter.getCount());
                Object currentGroup = new Object();
                int currentStart = 0;
                for (int position = 0; position < mAdapter.getCount(); ++position) {
                    Object group = mAdapter.getGroup(position);
                    // End of column or different group
                    if (position - currentStart == mNumColumns ||
                            !Objects.equals(currentGroup, group)) {
                        addRow(currentGroup, currentStart, position);
                        currentGroup = group;
                        currentStart = position;
                    }
                }
                addRow(currentGroup, currentStart, mAdapter.getCount());

                notifyDataSetChanged();

                if (!mRows.isEmpty() && mRealPositionOnUpdate != -1) {
                    setSelection(mRealPositionOnUpdate);
                }
            }
        }

        private void addRow(Object group, int start, int end) {
            if (start < end) {
                mRows.add(new Row(group, start, end));
            }
        }

        public void setSelection(int realPosition) {
            synchronized (mRowsLock) {
                if (mRows.isEmpty()) {
                    mRealPositionOnUpdate = realPosition;
                } else {
                    mRealPositionOnUpdate = -1;
                    GroupGridView.super.setSelection(getPositionFromRealPosition(realPosition));
                }
            }
        }

        public int getPositionFromRealPosition(int realPosition) {
            synchronized (mRowsLock) {
                for (int position = realPosition / mNumColumns; position < mRows.size(); ++position) {
                    if (realPosition < mRows.get(position).end) {
                        return position;
                    }
                }
                LOG.w("Could not find position from real position: " + realPosition);
                return 0;
            }
        }

        public int getRealPositionFromPosition(int position) {
            synchronized (mRowsLock) {
                return position < mRows.size() ? mRows.get(position).start : 0;
            }
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                Integer position = (Integer) v.getTag();
                mListener.onItemClick(GroupGridView.this, v, position);
            }
        }

        @Override
        public Object getGroup(int position) {
            synchronized (mRowsLock) {
                if (position < mRows.size()) {
                    return mRows.get(position).group;
                } else {
                    return null;
                }
            }
        }

        @Override
        public View getGroupView(Object group, View convertView, ViewGroup parent) {
            return mAdapter.getGroupView(group, convertView, parent);
        }

        @Override
        public int getCount() {
            synchronized (mRowsLock) {
                return mRows.size();
            }
        }

        @Override
        public Object getItem(int position) {
            synchronized (mRowsLock) {
                if (position < mRows.size()) {
                    return mRows.get(position);
                } else {
                    return null;
                }
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Row row;
            synchronized (mRowsLock) {
                row = mRows.get(position);
            }
            int count = row.end - row.start;

            LinearLayout newView = new LinearLayout(getContext());
            // Distribution of weight if calculated by taking the total width minus all spacing,
            // including all margin and padding of the view and its children. Thus, if the row is
            // not completely filled, we add more padding to account for the margin of the missing
            // children.
            int rightPadding = mHorizontalSpacing * (1 + mNumColumns - count);
            newView.setPadding(0, 0, rightPadding, mVerticalSpacing);
            newView.setWeightSum(mNumColumns);

            LinearLayout oldView = (LinearLayout) convertView;
            int oldViewChildCount = oldView != null ? oldView.getChildCount() : 0;

            for (int i = 0; i < count; ++i) {
                int realPosition = row.start + i;
                View childConvertView = i < oldViewChildCount ? oldView.getChildAt(i) : null;
                View childView = mAdapter.getView(realPosition, childConvertView, newView);
                if (childView.getParent() != null) {
                    ((ViewGroup) childView.getParent()).removeView(childView);
                }
                childView.setTag(realPosition);
                childView.setOnClickListener(this);
                newView.addView(childView, createChildLayoutParams());
            }
            return newView;
        }

        private LinearLayout.LayoutParams createChildLayoutParams() {
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = mHorizontalSpacing;
            params.weight = 1;
            return params;
        }

    }

}
