package com.jaslong.hscompanion.app.deck.detail;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.jaslong.hscompanion.util.Logger;
import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.app.HearthstoneActivity;
import com.jaslong.hscompanion.app.deck.construction.ConstructionFragment;
import com.jaslong.hscompanion.app.deck.construction.DeckConstructor;
import com.jaslong.hscompanion.app.detail.CardDetailFragment;
import com.jaslong.hscompanion.card.DeckManager;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.Deck;
import com.jaslong.hscompanion.model.DeckCard;
import com.jaslong.hscompanion.model.HeroClass;
import com.jaslong.util.android.app.BaseFragment;
import com.jaslong.util.android.app.CachedAsyncTaskLoader;

import java.util.List;

public class DeckDetailFragment extends BaseFragment implements
        DeckCardsFragment.Callback,
        DeckConfirmDeleteFragment.Callback,
        DeckNameFragment.Callback {

    private static final Logger sLogger = Logger.create("Deck", "DeckDetailFragment");

    public static DeckDetailFragment createInstance(long deckId) {
        Bundle state = new Bundle();
        state.putLong(STATE_DECK_ID, deckId);
        DeckDetailFragment fragment = new DeckDetailFragment();
        fragment.setArguments(state);
        return fragment;
    }

    private static final String STATE_DECK_ID = "deck_id";
    private static final String STATE_EDITED = "edited";

    private long mDeckId;
    private String mDeckName;
    private HeroClass mDeckClass;
    private DeckManager mDeckManager;

    private ContentObserver mDeckObserver;
    private ContentObserver mDeckCardsObserver;

    private DeckCardsFragment mDeckCardsFragment;
    private DeckStatsFragment mDeckStatsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mDeckId = getState().getLong(STATE_DECK_ID);

        mDeckManager = new DeckManager(getActivity());

        AsyncTaskLoader<Deck> deckLoader = (AsyncTaskLoader<Deck>)
                getLoaderManager().initLoader(0, null, new DeckLoaderCallbacks());
        AsyncTaskLoader<List<DeckCard>> deckCardsLoader = (AsyncTaskLoader<List<DeckCard>>)
                getLoaderManager().initLoader(1, null, new DeckCardsLoaderCallbacks());

        mDeckObserver = deckLoader.new ForceLoadContentObserver();
        mDeckCardsObserver = deckCardsLoader.new ForceLoadContentObserver();

        Uri deckUri = Uri.withAppendedPath(DeckManager.DECKS_URI, String.valueOf(mDeckId));
        Uri deckCardsUri = Uri.withAppendedPath(DeckManager.DECK_CARD_URI, String.valueOf(mDeckId));
        getActivity().getContentResolver().registerContentObserver(
                deckUri,
                true,
                mDeckObserver);
        getActivity().getContentResolver().registerContentObserver(
                deckCardsUri,
                true,
                mDeckCardsObserver);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.deck_detail_fragment, container, false);

        mDeckCardsFragment = (DeckCardsFragment)
                getChildFragmentManager().findFragmentById(R.id.cards_frame);
        mDeckStatsFragment = (DeckStatsFragment)
                getChildFragmentManager().findFragmentById(R.id.stats_frame);
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        if (mDeckCardsFragment == null) {
            mDeckCardsFragment = DeckCardsFragment.createInstance(0);
            fragmentTransaction.add(R.id.cards_frame, mDeckCardsFragment);
        }
        if (mDeckStatsFragment == null) {
            mDeckStatsFragment = new DeckStatsFragment();
            fragmentTransaction.add(R.id.stats_frame, mDeckStatsFragment);
        }
        fragmentTransaction.commit();

        return view;
    }

    @Override
    public void onDestroy() {
        getActivity().getContentResolver().unregisterContentObserver(mDeckObserver);
        getActivity().getContentResolver().unregisterContentObserver(mDeckCardsObserver);
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.deck_detail_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_button:
                edit();
                return true;
            case R.id.rename_button:
                rename();
                return true;
            case R.id.delete_button:
                delete();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDeckCardClicked(Card card) {
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_left_enter, R.anim.slide_left_exit,
                        R.anim.slide_right_enter, R.anim.slide_right_exit)
                .add(R.id.content_frame, CardDetailFragment.createInstance(card.getCardId()))
                .remove(this)
                .addToBackStack("deck to detail")
                .commit();
    }

    @Override
    public void onConfirmDelete() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (!mDeckManager.deleteDeck(mDeckId)) {
                    sLogger.w("Failed to delete deck!");
                }
                return null;
            }
        }.execute();
        getFragmentManager().popBackStack();
    }

    @Override
    public void onDeckName(final String newDeckName) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (!mDeckManager.renameDeck(mDeckId, newDeckName)) {
                    sLogger.w("Failed to rename deck!");
                }
                return null;
            }
        }.execute();
    }

    private void edit() {
        getState().putBoolean(STATE_EDITED, true);
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_left_enter, R.anim.slide_left_exit,
                        R.anim.slide_right_enter, R.anim.slide_right_exit)
                .add(R.id.content_frame,
                        ConstructionFragment.createInstance(mDeckId, mDeckClass))
                .remove(this)
                .addToBackStack("deck detail to construction")
                .commit();
    }

    private void rename() {
        getChildFragmentManager()
                .beginTransaction()
                .add(DeckNameFragment.create(mDeckName), DeckNameFragment.TAG)
                .commit();
    }

    private void delete() {
        getChildFragmentManager()
                .beginTransaction()
                .add(new DeckConfirmDeleteFragment(), DeckConfirmDeleteFragment.TAG)
                .commit();
    }

    private class DeckLoaderCallbacks implements LoaderManager.LoaderCallbacks<Deck> {
        @Override
        public Loader<Deck> onCreateLoader(int id, Bundle bundle) {
            return new CachedAsyncTaskLoader<Deck>(getActivity()) {
                @Override
                public Deck loadInBackground() {
                    return mDeckManager.getDeck(mDeckId);
                }
            };
        }
        @Override
        public void onLoadFinished(Loader<Deck> loader, Deck deck) {
            mDeckName = deck.getName();
            mDeckClass = deck.getDeckClass();
            HearthstoneActivity activity = (HearthstoneActivity) getActivity();
            activity.getSupportActionBar().setTitle(mDeckName);
            activity.setTheme(mDeckClass);
        }
        @Override
        public void onLoaderReset(Loader<Deck> loader) {
        }
    }

    private class DeckCardsLoaderCallbacks
            implements LoaderManager.LoaderCallbacks<List<DeckCard>> {
        @Override
        public Loader<List<DeckCard>> onCreateLoader(int id, Bundle bundle) {
            return new CachedAsyncTaskLoader<List<DeckCard>>(getActivity()) {
                @Override
                public List<DeckCard> loadInBackground() {
                    return mDeckManager.getDeckCards(mDeckId);
                }
            };
        }
        @Override
        public void onLoadFinished(Loader<List<DeckCard>> loader, List<DeckCard> deckCardList) {
            DeckConstructor constructor = new DeckConstructor(mDeckManager, mDeckId, deckCardList);
            List<DeckCard> deckCards = constructor.getDeckCards();
            mDeckCardsFragment.swapList(deckCards);
            mDeckStatsFragment.swapList(deckCards);
            if (deckCardList.isEmpty() && !getState().getBoolean(STATE_EDITED)) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        edit();
                    }
                });
            }
        }
        @Override
        public void onLoaderReset(Loader<List<DeckCard>> loader) {
        }
    }

}
