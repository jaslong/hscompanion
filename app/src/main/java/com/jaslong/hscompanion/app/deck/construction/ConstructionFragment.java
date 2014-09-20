package com.jaslong.hscompanion.app.deck.construction;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.app.HearthstoneActivity;
import com.jaslong.hscompanion.app.collection.CollectionControllerFragment;
import com.jaslong.hscompanion.app.collection.ICollectionFragment;
import com.jaslong.hscompanion.app.deck.detail.DeckCardsFragment;
import com.jaslong.hscompanion.app.deck.detail.DeckStatsFragment;
import com.jaslong.hscompanion.card.DeckManager;
import com.jaslong.hscompanion.card.Util;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.DeckCard;
import com.jaslong.hscompanion.model.HeroClass;
import com.jaslong.util.android.app.BaseFragment;
import com.jaslong.util.android.app.CachedAsyncTaskLoader;

import java.util.List;

public class ConstructionFragment extends BaseFragment implements
        ICollectionFragment.Callback,
        DeckCardsFragment.Callback {

    public static ConstructionFragment createInstance(long deckId, HeroClass heroClass) {
        Bundle state = new Bundle();
        state.putLong(STATE_DECK_ID, deckId);
        state.putString(STATE_HERO_CLASS, heroClass.toString());
        ConstructionFragment fragment = new ConstructionFragment();
        fragment.setArguments(state);
        return fragment;
    }

    private static final String STATE_DECK_ID = "deck_id";
    private static final String STATE_HERO_CLASS = "hero_class";

    private static final int PAGE_GRID = 0;
    private static final int PAGE_DECK = 1;

    private DeckManager mDeckManager;

    private DeckConstructor mDeckConstructor;

    private ConstructionPagerAdapter mAdapter;
    private ViewPager mPager;
    private DeckStatsFragment mDeckStatsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mDeckManager = new DeckManager(getActivity());
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.construction_fragment, container, false);

        mAdapter = new ConstructionPagerAdapter();

        mPager = (ViewPager) view.findViewById(R.id.view_pager);
        mPager.setPageMargin(1);
        mPager.setPageMarginDrawable(R.color.divider_color);
        mPager.setAdapter(mAdapter);

        mDeckStatsFragment = (DeckStatsFragment)
                getChildFragmentManager().findFragmentById(R.id.stats_frame);
        if (mDeckStatsFragment == null) {
            mDeckStatsFragment = new DeckStatsFragment();
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.stats_frame, mDeckStatsFragment)
                    .commit();
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(PAGE_GRID, null, new DeckCardsLoaderCallbacks());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.construction_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done_button:
                getFragmentManager().popBackStack();
                return true;
        }
        return false;
    }

    @Override
    public void onCardClicked(final Card card, int position) {
        if (mDeckConstructor.addCard(card)) {
            update();
        }
    }

    @Override
    public void onHeroClassChanged(HeroClass heroClass) {
    }

    @Override
    public void onDeckCardClicked(Card card) {
        if (mDeckConstructor.removeCard(card)) {
            update();
        }
    }

    private void update() {
        List<DeckCard> deckCards = mDeckConstructor.getDeckCards();
        getDeckCardsFragment().swapList(deckCards);
        mDeckStatsFragment.swapList(deckCards);
    }

    private DeckCardsFragment getDeckCardsFragment() {
        return (DeckCardsFragment) mAdapter.getFragment(mPager, PAGE_DECK);
    }

    private class ConstructionPagerAdapter extends FragmentPagerAdapter {

        private final SparseArray<Fragment> mFragments;

        public ConstructionPagerAdapter() {
            super(getChildFragmentManager());
            mFragments = new SparseArray<>(getCount());
        }

        public Fragment getFragment(ViewGroup container, int position) {
            Fragment fragment = mFragments.get(position);
            if (fragment != null) {
                return fragment;
            } else {
                return (Fragment) instantiateItem(container, position);
            }
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                case PAGE_GRID:
                    fragment = CollectionControllerFragment.createInstance(
                            Util.toEnum(HeroClass.class, getState().getString(STATE_HERO_CLASS)),
                            null);
                    break;
                case PAGE_DECK:
                    fragment = DeckCardsFragment.createInstance(0);
                    break;
                default:
                    throw new IllegalStateException();
            }
            mFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public float getPageWidth(int position) {
            return .75f;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            mFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mFragments.remove(position);
            super.destroyItem(container, position, object);
        }

    }

    private class DeckCardsLoaderCallbacks
            implements LoaderManager.LoaderCallbacks<List<DeckCard>> {
        @Override
        public Loader<List<DeckCard>> onCreateLoader(int id, Bundle bundle) {
            return new CachedAsyncTaskLoader<List<DeckCard>>(getActivity()) {
                @Override
                public List<DeckCard> loadInBackground() {
                    return mDeckManager.getDeckCards(getState().getLong(STATE_DECK_ID));
                }
            };
        }
        @Override
        public void onLoadFinished(Loader<List<DeckCard>> loader, List<DeckCard> deckCardList) {
            mDeckConstructor = new DeckConstructor(
                    mDeckManager,
                    getState().getLong(STATE_DECK_ID),
                    deckCardList);
            update();
            // Set theme so that the deck stats fragment has theme colors.
            ((HearthstoneActivity) getActivity()).setTheme(
                    Util.toEnum(HeroClass.class, getState().getString(STATE_HERO_CLASS)));
        }
        @Override
        public void onLoaderReset(Loader<List<DeckCard>> loader) {
        }
    }

}
