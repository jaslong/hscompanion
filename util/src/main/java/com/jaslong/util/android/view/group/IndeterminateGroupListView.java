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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.jaslong.util.Objects;
import com.jaslong.util.android.log.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * ListView with groupings. This view does not need to loop through all items of the adapter, so it
 * can be used with indeterminate data.
 */
public class IndeterminateGroupListView extends RelativeLayout {

    public interface OnGroupChangeListener {
        void onGroupChanged(IndeterminateGroupListView view, Object group);
    }

    private static final Logger LOG = new Logger("jUtil", "GroupListView");

    protected final ListView mListView;
    private final FrameLayout mFixedGroupView;
    private InternalAdapter mAdapter;
    private OnGroupChangeListener mGroupChangeListener;

    public IndeterminateGroupListView(Context context) {
        this(context, null);
    }

    public IndeterminateGroupListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndeterminateGroupListView(Context context, AttributeSet attrs, int defStyle) {
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
        GroupListAdapter providedAdapter = provideAdapter(adapter);
        mAdapter = new InternalAdapter(getContext(), providedAdapter);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(new OnScrollListener(providedAdapter));
    }

    protected GroupListAdapter provideAdapter(GroupListAdapter adapter) {
        return adapter;
    }

    protected void invalidateViews() {
        mListView.invalidateViews();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mListView.setOnItemClickListener(listener);
    }

    public int getFirstVisiblePosition() {
        return mListView.getFirstVisiblePosition();
    }

    public int getFirstVisibleTop() {
        View firstChild = mListView.getChildAt(0);
        return firstChild != null ? firstChild.getTop() : 0;
    }

    public void setSelection(final int position) {
        if (mAdapter.isGroupStart(position) != null) {
            // Position is the start of a group, so we don't need to shift by the group view height.
            doSetSelectionFromTop(position, 0);
        } else {
            // Shift by the group view height so the first view is not blocked by the group view.
            mListView.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (mFixedGroupView != null && mFixedGroupView.getHeight() != 0) {
                                doSetSelectionFromTop(position, mFixedGroupView.getHeight());
                                mListView.getViewTreeObserver()
                                        .removeGlobalOnLayoutListener(this);
                            }
                        }
                    });
        }
    }

    public void setSelectionFromTop(int position, int y) {
        doSetSelectionFromTop(position, y);
    }

    private void doSetSelectionFromTop(final int position, final int y) {
        mListView.setSelectionFromTop(position, y);
        mListView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        // Check that the selected item is in view.
                        if (mListView.getCount() == 0 ||
                                (mListView.getFirstVisiblePosition() <= position
                                        && position <= mListView.getLastVisiblePosition())) {
                            mListView.getViewTreeObserver().removeOnPreDrawListener(this);
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
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

    private class OnScrollListener implements AbsListView.OnScrollListener {

        private final GroupListAdapter mAdapter;
        private Object mCurrentGroup;

        public OnScrollListener(GroupListAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(
                AbsListView view,
                int firstVisibleItem,
                int visibleItemCount,
                int totalItemCount) {
            // Update group view
            Object group = mAdapter.getGroup(firstVisibleItem);
            if (!Objects.equals(mCurrentGroup, group) ||
                    // The view is not drawn the first time. Is this a bug?
                    (mFixedGroupView != null && mFixedGroupView.getHeight() == 0)) {
                LOG.i("Updating group: " + group);
                mCurrentGroup = group;

                // Add new group view if applicable.
                View groupViewChild = mFixedGroupView.getChildAt(0);
                if (group == null) {
                    mFixedGroupView.removeAllViews();
                } else {
                    View groupView = mAdapter.getGroupView(
                            group,
                            groupViewChild,
                            IndeterminateGroupListView.this);
                    if (groupView != groupViewChild) {
                        mFixedGroupView.removeAllViews();
                        mFixedGroupView.addView(groupView);
                    }
                }

                if (mGroupChangeListener != null) {
                    mGroupChangeListener.onGroupChanged(IndeterminateGroupListView.this, mCurrentGroup);
                }
            }

            // Align group view bottom with second group view top
            LinearLayout secondChild = (LinearLayout) view.getChildAt(1);
            if (secondChild == null) {
                return;
            }

            LayoutParams layoutParams = (LayoutParams) mFixedGroupView.getLayoutParams();
            int topMargin = 0;
            if (hasGroupView(secondChild)) {
                int firstGroupViewBottom = mFixedGroupView.getBottom();
                int secondGroupViewTop = secondChild.getTop();
                if (secondGroupViewTop < mFixedGroupView.getHeight()) {
                    int diff = secondGroupViewTop - firstGroupViewBottom;
                    topMargin = layoutParams.topMargin + diff;
                }
            }
            if (layoutParams.topMargin != topMargin) {
                LayoutParams newLayoutParams = new LayoutParams((MarginLayoutParams) layoutParams);
                newLayoutParams.topMargin = topMargin;
                mFixedGroupView.setLayoutParams(newLayoutParams);
            }
        }
    }

    private static boolean hasGroupView(ViewGroup view) {
        return view.getChildCount() == 2;
    }

    private static boolean hasItemView(ViewGroup view) {
        return view.getChildCount() == 1;
    }

    private static class InternalAdapter implements ListAdapter {

        private final Context mContext;
        private final GroupListAdapter mAdapter;
        private final Map<Object, Integer> mGroupStartPositions;

        public InternalAdapter(Context context, GroupListAdapter adapter) {
            mContext = context;
            mAdapter = adapter;
            mGroupStartPositions = new HashMap<>();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout view = (LinearLayout) convertView;
            if (convertView == null) {
                view = new LinearLayout(mContext);
                view.setOrientation(LinearLayout.VERTICAL);
                view.setLayoutParams(new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            View groupView = null;
            View itemView = null;
            if (hasGroupView(view)) {
                groupView = view.getChildAt(0);
                itemView = view.getChildAt(1);
            } else if (hasItemView(view)) {
                itemView = view.getChildAt(0);
            }
            view.removeAllViews();

            Object group = isGroupStart(position);
            if (group != null) {
                groupView = mAdapter.getGroupView(group, groupView, view);
                if (groupView != null) {
                    view.addView(groupView);
                }
            }

            itemView = mAdapter.getView(position, itemView, view);
            view.addView(itemView);

            return view;
        }

        private Object isGroupStart(int position) {
            Object group = mAdapter.getGroup(position);
            Integer startPosition = mGroupStartPositions.get(group);
            if (startPosition != null && startPosition == position) {
                return group;
            } else {
                return checkIfIsGroupStart(position, group);
            }
        }

        private Object checkIfIsGroupStart(int position, Object group) {
            if (position == 0) {
                LOG.ic("Start position 0 for group " + group + " is " + position);
                mGroupStartPositions.put(group, position);
                return group;
            } else {
                Object previousGroup = mAdapter.getGroup(position - 1);
                if (Objects.equals(group, previousGroup)) {
                    return null;
                } else {
                    LOG.ic("Start position for group " + group + " is " + position);
                    mGroupStartPositions.put(group, position);
                    return group;
                }
            }
        }

        // SUPER METHODS

        @Override
        public boolean areAllItemsEnabled() {
            return mAdapter.areAllItemsEnabled();
        }

        @Override
        public boolean isEnabled(int position) {
            return mAdapter.isEnabled(position);
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            mAdapter.registerDataSetObserver(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            mAdapter.unregisterDataSetObserver(observer);
        }

        @Override
        public int getCount() {
            return mAdapter.getCount();
        }

        @Override
        public Object getItem(int position) {
            return mAdapter.getItem(position);
        }

        @Override
        public long getItemId(int position) {
            return mAdapter.getItemId(position);
        }

        @Override
        public boolean hasStableIds() {
            return mAdapter.hasStableIds();
        }

        @Override
        public int getItemViewType(int position) {
            return mAdapter.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return mAdapter.getViewTypeCount();
        }

        @Override
        public boolean isEmpty() {
            return mAdapter.isEmpty();
        }
    }

}
