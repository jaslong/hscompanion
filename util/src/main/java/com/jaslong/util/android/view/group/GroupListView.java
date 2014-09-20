package com.jaslong.util.android.view.group;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.jaslong.util.Objects;
import com.jaslong.util.android.log.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * ListView with groupings.
 */
public class GroupListView extends RelativeLayout {

    public interface OnGroupChangeListener {
        void onGroupChanged(GroupListView view, Object group);
    }

    private static final Logger sLogger = new Logger("jUtil", "GroupListView");

    protected final ListView mListView;
    private final FrameLayout mFixedGroupView;
    private GroupListAdapter mGroupListAdapter;
    private InternalAdapter mInternalAdapter;
    private OnGroupChangeListener mGroupChangeListener;

    public GroupListView(Context context) {
        this(context, null);
    }

    public GroupListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GroupListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mListView = new ListView(getContext(), attrs, defStyle);
        mListView.setId(android.R.id.list); // set an id so it can save its state
        // TODO: Support over-scroll. Group view needs to adjusted when over-scrolled.
        mListView.setOverScrollMode(OVER_SCROLL_NEVER);
        mFixedGroupView = new FrameLayout(getContext());

        addView(mListView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mFixedGroupView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    public void setAdapter(GroupListAdapter adapter) {
        mGroupListAdapter = provideAdapter(adapter);
        mInternalAdapter = new InternalAdapter(mGroupListAdapter);
        mListView.setAdapter(mInternalAdapter);
        mListView.setOnScrollListener(new OnScrollListener());
    }

    protected GroupListAdapter provideAdapter(GroupListAdapter adapter) {
        return adapter;
    }

    protected void invalidateViews() {
        mListView.invalidateViews();
    }

    public void setOnItemClickListener(final AdapterView.OnItemClickListener listener) {
        if (listener == null) {
            mListView.setOnItemClickListener(null);
        } else {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    listener.onItemClick(
                            parent, view, mInternalAdapter.toActualPosition(position), id);
                }
            });
        }
    }

    public int getFirstVisiblePosition() {
        return mListView.getFirstVisiblePosition();
    }

    public int getFirstVisibleTop() {
        View firstChild = mListView.getChildAt(0);
        return firstChild != null ? firstChild.getTop() : 0;
    }

    public void setSelection(final int position) {
        doSetSelectionFromTop(position, 0);
    }

    public void setSelectionFromTop(int position, int y) {
        doSetSelectionFromTop(position, y);
    }

    private void doSetSelectionFromTop(final int position, final int y) {
        mListView.setSelectionFromTop(position, y);
        if (position > 0) {
            mListView.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            // Check that the selected item is in view.
                            if (mListView.getCount() == 0
                                    || mListView.getFirstVisiblePosition() > 0) {
                                mListView.getViewTreeObserver().removeOnPreDrawListener(this);
                                return true;
                            } else {
                                return false;
                            }
                        }
                    });
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setFastScrollAlwaysVisible(boolean alwaysShow) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mListView.setFastScrollAlwaysVisible(alwaysShow);
        }
    }

    public void setFastScrollEnabled(boolean enabled) {
        mListView.setFastScrollEnabled(enabled);
    }

    public void setDivider(Drawable divider) {
        mListView.setDivider(divider);
    }

    public void setDividerHeight(int height) {
        mListView.setDividerHeight(height);
    }

    public void setOnGroupChangeListener(OnGroupChangeListener listener) {
        mGroupChangeListener = listener;
    }

    public void triggerGroupChangeListener() {
        InternalItem item = mInternalAdapter.getInternalItem(mListView.getFirstVisiblePosition());
        if (item != null) {
            Object group = item.isGroup() ?
                    item.getItem() : mGroupListAdapter.getGroup(item.getActualPosition());
            mGroupChangeListener.onGroupChanged(this, group);
        } else {
            mGroupChangeListener.onGroupChanged(this, null);
        }
    }

    private class OnScrollListener implements AbsListView.OnScrollListener {

        private Object mCurrentGroup;

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(
                AbsListView view,
                int firstVisibleItem,
                int visibleItemCount,
                int totalItemCount) {
            if (totalItemCount == 0) {
                return;
            }

            // The FrameLayout's height sometimes stays 0. Is this a bug?
            if (mFixedGroupView.getHeight() == 0) {
                mFixedGroupView.requestLayout();
            }

            // Update group view
            InternalItem item = mInternalAdapter.getInternalItem(firstVisibleItem);
            Object group = item.isGroup() ?
                    item.getItem() : mGroupListAdapter.getGroup(item.getActualPosition());
            if (!Objects.equals(mCurrentGroup, group)) {
                sLogger.i("Updating group: " + group);
                mCurrentGroup = group;

                // Add new group view if applicable.
                View groupViewChild = mFixedGroupView.getChildAt(0);
                if (group == null) {
                    mFixedGroupView.removeAllViews();
                } else {
                    View groupView = mGroupListAdapter.getGroupView(
                            group,
                            groupViewChild,
                            GroupListView.this);
                    if (groupView != groupViewChild) {
                        mFixedGroupView.removeAllViews();
                        mFixedGroupView.addView(groupView);
                    }
                }

                if (mGroupChangeListener != null) {
                    mGroupChangeListener.onGroupChanged(GroupListView.this, mCurrentGroup);
                }
            }

            // Find position of next group, if visible.
            LayoutParams layoutParams = (LayoutParams) mFixedGroupView.getLayoutParams();
            int topMargin = 0;
            for (int i = 1; i < visibleItemCount; ++i) {
                View childView = mListView.getChildAt(i);
                if (childView == null) {
                    break;
                }
                int childViewTop = childView.getTop();
                int fixedGroupViewHeight = mFixedGroupView.getHeight();

                if (childViewTop >= fixedGroupViewHeight) {
                    break;
                } else if (mInternalAdapter.getInternalItem(firstVisibleItem + i).isGroup()) {
                    topMargin = childViewTop - fixedGroupViewHeight;
                }
            }

            if (layoutParams.topMargin != topMargin) {
                LayoutParams newLayoutParams = new LayoutParams((MarginLayoutParams) layoutParams);
                newLayoutParams.topMargin = topMargin;
                mFixedGroupView.setLayoutParams(newLayoutParams);
            }
        }
    }

    private static class InternalItem {
        private static final int GROUP_POSITION = -1;
        private final Object mItem;
        private final int mActualPosition;
        private final boolean mIsGroup;
        public InternalItem(Object item, int actualPosition) {
            mItem = item;
            mActualPosition = actualPosition;
            mIsGroup = false;
        }
        public InternalItem(Object group) {
            mItem = group;
            mActualPosition = GROUP_POSITION;
            mIsGroup = true;
        }
        public Object getItem() {
            return mItem;
        }
        public int getActualPosition() {
            return mActualPosition;
        }
        public boolean isGroup() {
            return mIsGroup;
        }
    }

    private static class InternalAdapter extends BaseAdapter {

        private final GroupListAdapter mAdapter;
        private final Object mItemsLock = new Object();
        private List<InternalItem> mItems;

        public InternalAdapter(GroupListAdapter adapter) {
            mAdapter = adapter;
            mItems = Collections.emptyList();

            mAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    update();
                }

                @Override
                public void onInvalidated() {
                    synchronized (mItemsLock) {
                        mItems = Collections.emptyList();
                    }
                }
            });
            update();
        }

        private void update() {
            List<InternalItem> items = new LinkedList<>();
            Object currentGroup = null;
            for (int i = 0; i < mAdapter.getCount(); ++i) {
                Object group = mAdapter.getGroup(i);
                if (!Objects.equals(currentGroup, group)) {
                    currentGroup = group;
                    items.add(new InternalItem(currentGroup));
                }
                items.add(new InternalItem(mAdapter.getItem(i), i));
            }
            synchronized (mItemsLock) {
                mItems = Collections.unmodifiableList(new ArrayList<>(items));
            }
            notifyDataSetChanged();
        }

        public InternalItem getInternalItem(int position) {
            synchronized (mItemsLock) {
                if (position < mItems.size()) {
                    return mItems.get(position);
                } else {
                    return null;
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            InternalItem item = getInternalItem(position);
            if (item.isGroup()) {
                return mAdapter.getGroupView(item.getItem(), convertView, parent);
            } else {
                return mAdapter.getView(item.getActualPosition(), convertView, parent);
            }
        }

        @Override
        public int getCount() {
            synchronized (mItemsLock) {
                return mItems.size();
            }
        }

        @Override
        public Object getItem(int position) {
            return getInternalItem(position).getItem();
        }

        @Override
        public boolean isEnabled(int position) {
            int actualPosition = toActualPosition(position);
            return actualPosition != InternalItem.GROUP_POSITION
                    && mAdapter.isEnabled(actualPosition);
        }

        @Override
        public long getItemId(int position) {
            int actualPosition = toActualPosition(position);
            if (actualPosition == InternalItem.GROUP_POSITION) {
                return -1L;
            } else {
                return mAdapter.getItemId(actualPosition);
            }
        }

        @Override
        public int getItemViewType(int position) {
            int actualPosition = toActualPosition(position);
            if (actualPosition == InternalItem.GROUP_POSITION) {
                return mAdapter.getViewTypeCount();
            } else {
                return mAdapter.getItemViewType(actualPosition);
            }
        }

        @Override
        public int getViewTypeCount() {
            return mAdapter.getViewTypeCount() + 1;
        }

        private int toActualPosition(int position) {
            return getInternalItem(position).getActualPosition();
        }

        // SUPER METHODS

        @Override
        public boolean areAllItemsEnabled() {
            return mAdapter.areAllItemsEnabled();
        }

        @Override
        public boolean hasStableIds() {
            return mAdapter.hasStableIds();
        }

    }

}
