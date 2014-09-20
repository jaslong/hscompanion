package com.jaslong.hscompanion.app.collection;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.HeroClass;
import com.jaslong.util.android.app.BaseFragment;
import com.jaslong.util.android.view.group.GroupListView;

import java.util.List;

public class ListCollectionFragment extends BaseFragment implements ICollectionFragment {

    private static final String STATE_FIRST_VISIBLE_POSITION = "first_visible_position";
    private static final String STATE_FIRST_VISIBLE_TOP = "first_visible_top";

    public static ListCollectionFragment createInstance(int firstVisiblePosition) {
        Bundle state = new Bundle();
        state.putInt(STATE_FIRST_VISIBLE_POSITION, firstVisiblePosition);
        ListCollectionFragment fragment = new ListCollectionFragment();
        fragment.setArguments(state);
        return fragment;
    }

    private Callback mCallback;
    private GroupListView mListView;
    private CompactRowCardAdapter mAdapter;
    private List<Card> mList;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (Callback) getParentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new CompactRowCardAdapter(getActivity());
        if (mList != null) {
            mAdapter.swapList(mList);
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mListView = new GroupListView(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setDivider(new ColorDrawable(getResources().getColor(R.color.divider_color)));
        mListView.setDividerHeight(1);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallback.onCardClicked(mAdapter.getTypedItem(position), position);
            }
        });
        mListView.setOnGroupChangeListener(new GroupListView.OnGroupChangeListener() {
            @Override
            public void onGroupChanged(GroupListView view, Object group) {
                if (group instanceof HeroClass) {
                    mCallback.onHeroClassChanged((HeroClass) group);
                }
            }
        });

        if (getState().containsKey(STATE_FIRST_VISIBLE_TOP)) {
            mListView.setSelectionFromTop(
                    getState().getInt(STATE_FIRST_VISIBLE_POSITION),
                    getState().getInt(STATE_FIRST_VISIBLE_TOP));
        } else {
            mListView.setSelection(getState().getInt(STATE_FIRST_VISIBLE_POSITION));
        }

        return mListView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mListView != null) {
            getState().putInt(STATE_FIRST_VISIBLE_POSITION, mListView.getFirstVisiblePosition());
            getState().putInt(STATE_FIRST_VISIBLE_TOP, mListView.getFirstVisibleTop());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void swapList(List<Card> cardList) {
        mList = cardList;
        if (mAdapter != null) {
            mAdapter.swapList(mList);
        }
    }

    @Override
    public int getFirstVisiblePosition() {
        return mListView.getFirstVisiblePosition();
    }

    @Override
    public void triggerHeroClassChanged() {
        mListView.triggerGroupChangeListener();
    }

}
