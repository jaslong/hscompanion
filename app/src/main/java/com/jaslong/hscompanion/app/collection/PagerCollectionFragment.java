package com.jaslong.hscompanion.app.collection;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.app.HearthstoneActivity;
import com.jaslong.hscompanion.app.detail.CardDetailFragment;
import com.jaslong.hscompanion.util.ColorPicker;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.util.android.app.BaseFragment;

import java.util.List;

public class PagerCollectionFragment extends BaseFragment
        implements ICollectionFragment, ViewPager.OnPageChangeListener {

    private static final String STATE_POSITION = "position";

    private ViewPager mPager;
    private List<Card> mCardList;

    public static PagerCollectionFragment createInstance(int position) {
        Bundle state = new Bundle();
        state.putInt(STATE_POSITION, position);
        PagerCollectionFragment fragment = new PagerCollectionFragment();
        fragment.setArguments(state);
        return fragment;
    }

    public PagerCollectionFragment() { }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mPager = (ViewPager) inflater.inflate(R.layout.pager_collection_fragment, container, false);
        return mPager;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPager.setOnPageChangeListener(this);
        if (mCardList != null) {
            updateAdapter();
        }
    }

    @Override
    public void swapList(List<Card> cardList) {
        mCardList = cardList;
        if (mPager != null) {
            updateAdapter();
        }
    }

    @Override
    public int getFirstVisiblePosition() {
        return mPager.getCurrentItem();
    }

    @Override
    public void triggerHeroClassChanged() {
        onPageSelected(getState().getInt(STATE_POSITION));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        getState().putInt(STATE_POSITION, position);
        if (position >= mCardList.size()) {
            return;
        }
        Card card = mCardList.get(position);
        int backgroundColor = ColorPicker.getColor(card.getHeroClass(), getResources());

        PagerTitleStrip titleStrip = (PagerTitleStrip) mPager.findViewById(R.id.pager_title_strip);
        titleStrip.setBackgroundColor(backgroundColor);

        HearthstoneActivity activity = (HearthstoneActivity) getActivity();
        activity.setTheme(card.getHeroClass());
        activity.getSupportActionBar().setTitle(card.getName());
    }

    @Override
    public void onPageScrollStateChanged(int position) {
    }

    private void updateAdapter() {
        int position = getState().getInt(STATE_POSITION);
        PagerAdapter adapter = new PagerAdapter(getChildFragmentManager());
        mPager.setAdapter(adapter);
        mPager.setCurrentItem(position);
        mPager.setOffscreenPageLimit(2);
        onPageSelected(position);
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return CardDetailFragment.createInstance(mCardList.get(position).getCardId(), false);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mCardList.get(position).getName();
        }

        @Override
        public int getCount() {
            return mCardList.size();
        }
    }

}
