package com.jaslong.hscompanion.app.deck.list;

import android.database.ContentObserver;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.app.HearthstoneActivity;
import com.jaslong.hscompanion.app.deck.detail.DeckDetailFragment;
import com.jaslong.hscompanion.card.DeckManager;
import com.jaslong.hscompanion.model.Deck;
import com.jaslong.hscompanion.model.HeroClass;
import com.jaslong.util.android.app.BaseFragment;
import com.jaslong.util.android.app.CachedAsyncTaskLoader;
import com.jaslong.util.android.view.group.GroupListView;

import java.util.List;

public class DeckListFragment extends BaseFragment implements
        LoaderManager.LoaderCallbacks<List<Deck>>,
        AdapterView.OnItemClickListener,
        DeckAddFragment.Callback {

    public static final String TAG = "deck_list";

    private static final String STATE_FIRST_VISIBLE_POSITION = "first_visible_position";
    private static final String STATE_FIRST_VISIBLE_TOP = "first_visible_top";

    private Handler mHandler;
    private DeckManager mDeckManager;
    private DeckAdapter mAdapter;
    private ContentObserver mDeckListObserver;

    private GroupListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        mDeckManager = new DeckManager(getActivity());
        mAdapter = new DeckAdapter(getActivity());

        Loader<List<Deck>> loader = getLoaderManager().initLoader(0, null, this);

        mDeckListObserver = loader.new ForceLoadContentObserver();
        getActivity().getContentResolver().registerContentObserver(
                DeckManager.DECKS_URI,
                true,
                mDeckListObserver);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.deck_list_fragment, container, false);

        mListView = (GroupListView) view.findViewById(R.id.deck_list);
        mListView.setAdapter(mAdapter);
        mListView.setDivider(new ColorDrawable(getResources().getColor(R.color.divider_color)));
        mListView.setDividerHeight(1);
        mListView.setOnItemClickListener(this);
        mListView.setOnGroupChangeListener(new GroupListView.OnGroupChangeListener() {
            @Override
            public void onGroupChanged(GroupListView view, Object group) {
                if (group instanceof HeroClass) {
                    ((HearthstoneActivity) getActivity()).setTheme((HeroClass) group);
                }
            }
        });

        view.findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getChildFragmentManager()
                        .beginTransaction()
                        .add(new DeckAddFragment(), DeckAddFragment.TAG)
                        .commit();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((HearthstoneActivity) getActivity())
                .getSupportActionBar().setTitle(R.string.deck_list_title);
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
    public void onDestroy() {
        getActivity().getContentResolver().unregisterContentObserver(mDeckListObserver);
        super.onDestroy();
    }

    @Override
    public Loader<List<Deck>> onCreateLoader(int id, Bundle bundle) {
        return new CachedAsyncTaskLoader<List<Deck>>(getActivity()) {
            @Override
            public List<Deck> loadInBackground() {
                return mDeckManager.getDecks();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Deck>> loader, List<Deck> deckList) {
        boolean noDecks = deckList.isEmpty();
        ((HearthstoneActivity) getActivity()).setTheme(null);
        View view = getView();
        if (view != null) {
            view.findViewById(R.id.no_decks)
                    .setVisibility(noDecks ? View.VISIBLE : View.GONE);
            view.findViewById(R.id.deck_list)
                    .setVisibility(noDecks ? View.INVISIBLE : View.VISIBLE);
        }
        mAdapter.swapList(deckList);
        mListView.triggerGroupChangeListener();
    }

    @Override
    public void onLoaderReset(Loader<List<Deck>> loader) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showDeck(mAdapter.getTypedItem(position).getId());
    }

    @Override
    public void onDeckAdded(long deckId) {
        showDeck(deckId);
    }

    private void showDeck(final long deckId) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                getFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.slide_left_enter, R.anim.slide_left_exit,
                                R.anim.slide_right_enter, R.anim.slide_right_exit)
                        .add(R.id.content_frame, DeckDetailFragment.createInstance(deckId))
                        .remove(DeckListFragment.this)
                        .addToBackStack("deck to deck detail")
                        .commit();
            }
        });
    }

}
