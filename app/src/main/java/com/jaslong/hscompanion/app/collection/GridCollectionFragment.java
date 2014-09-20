package com.jaslong.hscompanion.app.collection;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.HeroClass;
import com.jaslong.util.android.app.BaseFragment;
import com.jaslong.util.android.view.group.GroupGridView;
import com.jaslong.util.android.view.group.GroupListView;

import java.util.List;

public class GridCollectionFragment extends BaseFragment
        implements ICollectionFragment, View.OnTouchListener {

    private static final String PREFS_FILE = "GridCollectionFragment";

    private static final String STATE_FIRST_VISIBLE_POSITION = "first_visible_position";
    private static final String STATE_ORIENTATION_UNDEFINED_NUM_COLUMNS = "undefined_num_columns";
    private static final String STATE_ORIENTATION_PORTRAIT_NUM_COLUMNS = "portrait_num_columns";
    private static final String STATE_ORIENTATION_LANDSCAPE_NUM_COLUMNS = "landscape_num_columns";
    private static final SparseArray<String> STATE_ORIENTATIONS;

    static {
        STATE_ORIENTATIONS = new SparseArray<>(2);
        STATE_ORIENTATIONS.put(
                Configuration.ORIENTATION_PORTRAIT,
                STATE_ORIENTATION_PORTRAIT_NUM_COLUMNS);
        STATE_ORIENTATIONS.put(
                Configuration.ORIENTATION_LANDSCAPE,
                STATE_ORIENTATION_LANDSCAPE_NUM_COLUMNS);
    }

    private static final int DEFAULT_WIDTH_PER_CARD = 100;
    private static final int MIN_WIDTH_PER_CARD = 70;

    public static GridCollectionFragment createInstance(int firstVisiblePosition) {
        Bundle state = new Bundle();
        state.putInt(STATE_FIRST_VISIBLE_POSITION, firstVisiblePosition);
        GridCollectionFragment fragment = new GridCollectionFragment();
        fragment.setArguments(state);
        return fragment;
    }

    private Callback mCallback;
    private GroupGridView mGridView;
    private ImageCardAdapter mAdapter;
    private List<Card> mCardList;
    private SharedPreferences mPrefs;
    private ScaleGestureDetector mScaleDetector;
    private int mOrientation;
    private int mWidthDp;
    private int mSpacing;
    private int mMaxNumColumns;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (Callback) getParentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mScaleDetector = new ScaleGestureDetector(getActivity(), new ScaleListener());
        mOrientation = getResources().getConfiguration().orientation;
        mWidthDp = (int) (displayMetrics.widthPixels / displayMetrics.density);
        mSpacing = getResources().getDimensionPixelOffset(R.dimen.grid_spacing);
        mMaxNumColumns = mWidthDp / MIN_WIDTH_PER_CARD;
        mAdapter = new ImageCardAdapter(getActivity(), displayMetrics.widthPixels, mSpacing);
        if (mCardList != null) {
            swapList(mCardList);
        }
        mPrefs = getActivity().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);

    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        int numColumns = getNumColumns();

        mGridView = new GroupGridView(getActivity());
        mGridView.setNumColumns(numColumns);
        mGridView.setHorizontalSpacing(mSpacing);
        mGridView.setVerticalSpacing(mSpacing);
        mGridView.setOnTouchListener(this);
        mGridView.setOnItemClickListener(new GroupGridView.OnItemClickListener() {
            @Override
            public void onItemClick(GroupGridView parent, View view, int position) {
                mCallback.onCardClicked(mAdapter.getTypedItem(position), position);
            }
        });
        mGridView.setOnGroupChangeListener(new GroupListView.OnGroupChangeListener() {
            @Override
            public void onGroupChanged(GroupListView view, Object group) {
                if (group instanceof HeroClass) {
                    mCallback.onHeroClassChanged((HeroClass) group);
                }
            }
        });

        mGridView.setAdapter(mAdapter);
        mGridView.setSelection(getState().getInt(STATE_FIRST_VISIBLE_POSITION));

        return mGridView;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mGridView != null) {
            getState().putInt(STATE_FIRST_VISIBLE_POSITION, mGridView.getFirstVisiblePosition());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        mPrefs.edit().putInt(getKeyForOrientation(), mGridView.getNumColumns()).apply();
        super.onPause();
    }

    @Override
    public void swapList(List<Card> cardList) {
        mCardList = cardList;
        if (mAdapter != null) {
            mAdapter.swapList(mCardList);
            triggerHeroClassChanged();
        }
    }

    @Override
    public int getFirstVisiblePosition() {
        return mGridView.getFirstVisiblePosition();
    }

    @Override
    public void triggerHeroClassChanged() {
        mGridView.triggerGroupChangeListener();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        return false;
    }

    private int getNumColumns() {
        String key = getKeyForOrientation();

        // Check SharedPreferences.
        int numColumns = mPrefs.getInt(key, 0);

        // Default.
        if (numColumns == 0) {
            numColumns = mWidthDp / DEFAULT_WIDTH_PER_CARD;
        }

        // Super default?
        if (numColumns == 0) {
            numColumns = 1;
        }

        return numColumns;
    }

    private String getKeyForOrientation() {
        return STATE_ORIENTATIONS.get(mOrientation, STATE_ORIENTATION_UNDEFINED_NUM_COLUMNS);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            int oldNumColumns = mGridView.getNumColumns();
            int newNumColumns = Math.max(Math.round(oldNumColumns / scaleFactor), 1);
            if (newNumColumns > mMaxNumColumns) {
                newNumColumns = mMaxNumColumns;
            }
            if (newNumColumns != oldNumColumns) {
                int position = mGridView.getFirstVisiblePosition();
                mGridView.setNumColumns(newNumColumns);
                mAdapter.setNumColumns(newNumColumns);
                mGridView.setSelection(position);
                return true;
            }
            return false;
        }
    }

}
