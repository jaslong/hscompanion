package com.jaslong.hscompanion.app.collection;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.app.HearthstoneActivity;
import com.jaslong.hscompanion.card.CardManager;
import com.jaslong.hscompanion.card.Util;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.HeroClass;
import com.jaslong.hscompanion.search.SearchUtils;
import com.jaslong.util.android.app.BaseFragment;
import com.jaslong.util.android.widget.CharacterTokenizer;
import com.jaslong.util.android.widget.DefaultAutoCompleteTextView;

import java.util.List;

public class CollectionControllerFragment extends BaseFragment implements
        LoaderManager.LoaderCallbacks<List<Card>>,
        ICollectionFragment.Callback {

    public static final String TAG = "collection";

    public static CollectionControllerFragment createInstance(HeroClass heroClass, String query) {
        Bundle state = new Bundle();
        state.putString(STATE_HERO_CLASS, heroClass != null ? heroClass.toString() : null);
        state.putString(STATE_QUERY, query);
        CollectionControllerFragment fragment = new CollectionControllerFragment();
        fragment.setArguments(state);
        return fragment;
    }

    private static final String STATE_HERO_CLASS = "hero_class";
    private static final String STATE_QUERY = "query";
    private static final String STATE_COLLECTION_FRAGMENT_CLASS = "collection_fragment_class";
    private static final String STATE_HIDDEN = "hidden";

    private static final String TAG_DETAIL = "detail";

    private ICollectionFragment.Callback mCallback;

    private ActionBar mActionBar;
    private InputMethodManager mInputMethodManager;
    private CardManager mCardManager;
    private ICollectionFragment mCollectionFragment;
    private List<Card> mCardList;

    private DefaultAutoCompleteTextView mSearchView;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getParentFragment() instanceof ICollectionFragment.Callback) {
            mCallback = (ICollectionFragment.Callback) getParentFragment();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInputMethodManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        mCardManager = (CardManager) getLoaderManager().initLoader(0 , null, this);
        if (isShowingSecrets()) {
            mCardManager.requestQuery(CardManager.QUERY_SECRETS);
        } else {
            requestSearch();
        }

        if (getState().getBoolean(STATE_HIDDEN)) {
            getFragmentManager().beginTransaction().hide(this).commit();
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.collection_controller_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Add search view to ActionBar
        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (isShowingSecrets()) {
            mActionBar.setTitle(R.string.menu_secrets);
        } else {
            mActionBar.setCustomView(R.layout.search_action_provider);
            mSearchView = (DefaultAutoCompleteTextView) mActionBar.getCustomView();
            initSearchView();
            mActionBar.setDisplayShowCustomEnabled(true);
            getActivity().supportInvalidateOptionsMenu();
        }

        // Load collection fragment
        String collectionFragmentClass = getState().getString(STATE_COLLECTION_FRAGMENT_CLASS);
        if (collectionFragmentClass == null) {
            collectionFragmentClass = GridCollectionFragment.class.getName();
        }
        mCollectionFragment = (ICollectionFragment)
                getChildFragmentManager().findFragmentById(R.id.collection_view);
        if (mCollectionFragment == null) {
            setFragment(collectionFragmentClass);
        }

        // Load ads, only if in construction
        if (getView() != null && getState().getString(STATE_HERO_CLASS) == null) {
            AdView mAdView = (AdView) getView().findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mActionBar.invalidateOptionsMenu();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        getState().putString(STATE_COLLECTION_FRAGMENT_CLASS,
                mCollectionFragment.getClass().getName());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        mActionBar.setDisplayShowCustomEnabled(false);
        mActionBar.setCustomView(null);
        super.onDestroyView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        getState().putBoolean(STATE_HIDDEN, hidden);
        if (hidden) {
            mActionBar.setDisplayShowCustomEnabled(false);
        } else {
            mActionBar.setDisplayShowCustomEnabled(true);
            if (isShowingSecrets()) {
                mActionBar.setTitle(R.string.menu_secrets);
            }
            mCollectionFragment.triggerHeroClassChanged();
        }
    }

    @Override
    public Loader<List<Card>> onCreateLoader(int id, Bundle bundle) {
        String heroClassName = getState().getString(STATE_HERO_CLASS);
        HeroClass heroClass = Util.toEnum(HeroClass.class, heroClassName);
        return new CardManager(getActivity(), heroClass);
    }

    @Override
    public void onLoadFinished(Loader<List<Card>> loader, List<Card> cardList) {
        mCardList = cardList;
        swapCardList();
    }

    @Override
    public void onLoaderReset(Loader<List<Card>> loader) {
    }

    @Override
    public void onCardClicked(Card card, int position) {
        hideKeyboard();
        if (mCallback != null) {
            mCallback.onCardClicked(card, position);
        } else {
            PagerCollectionFragment detailFragment = PagerCollectionFragment.createInstance(position);
            detailFragment.swapList(mCardList);
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_left_enter, R.anim.slide_left_exit,
                            R.anim.slide_right_enter, R.anim.slide_right_exit)
                    .add(R.id.content_frame, detailFragment, TAG_DETAIL)
                    .hide(this)
                    .addToBackStack("list to detail")
                    .commit();
        }
    }

    @Override
    public void onHeroClassChanged(HeroClass heroClass) {
        // Don't change the theme if the collection is limited to one class.
        if (getState().getString(STATE_HERO_CLASS) == null) {
            ((HearthstoneActivity) getActivity()).setTheme(heroClass);
        }
    }

    private void swapCardList() {
        if (mCardList == null) {
            return;
        }

        boolean noResults = mCardList.isEmpty();
        if (noResults) {
            ((HearthstoneActivity) getActivity()).setTheme(null);
        }
        if (getView() != null) {
            getView().findViewById(R.id.no_results)
                    .setVisibility(noResults ? View.VISIBLE : View.GONE);
            getView().findViewById(R.id.collection_view)
                    .setVisibility(noResults ? View.INVISIBLE : View.VISIBLE);
        }
        mCollectionFragment.swapList(mCardList);
        PagerCollectionFragment detailFragment = (PagerCollectionFragment)
                getFragmentManager().findFragmentByTag(TAG_DETAIL);
        if (detailFragment != null) {
            detailFragment.swapList(mCardList);
        }
    }

    private void setFragment(String collectionFragmentClass) {
        if (mCollectionFragment != null &&
                mCollectionFragment.getClass().getName().equals(collectionFragmentClass)) {
            return;
        }

        int firstVisiblePosition = mCollectionFragment != null ?
                mCollectionFragment.getFirstVisiblePosition() : 0;

        if (PagerCollectionFragment.class.getName().equals(collectionFragmentClass)) {
            mCollectionFragment = PagerCollectionFragment.createInstance(firstVisiblePosition);
        } else if (ListCollectionFragment.class.getName().equals(collectionFragmentClass)) {
            mCollectionFragment = ListCollectionFragment.createInstance(firstVisiblePosition);
        } else {
            mCollectionFragment = GridCollectionFragment.createInstance(firstVisiblePosition);
        }

        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.collection_view, (Fragment) mCollectionFragment)
                .commit();
        swapCardList();
    }

    private void initSearchView() {
        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                getState().putString(STATE_QUERY, s.toString());
                requestSearch();
            }
        });
        mSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId != EditorInfo.IME_NULL) {
                    hideKeyboard();
                    return true;
                } else {
                    return false;
                }
            }
        });
        mSearchView.setOnImeBackListener(new DefaultAutoCompleteTextView.OnImeBackListener() {
            @Override
            public void onImeBack(View view) {
                hideKeyboard();
            }
        });

        ArrayAdapter adapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                SearchUtils.KEYWORDS);
        ArrayAdapter defAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_dropdown_item_1line,
                new String[] {
                        Card.Set.NAXXRAMAS.toString(),
                        Card.Set.GOBLINS_VS_GNOMES.toString(),
                        Card.Rarity.LEGENDARY.toString()
                });
        mSearchView.setAdapter(adapter);
        mSearchView.setDefaultAdapter(defAdapter);
        mSearchView.setTokenizer(new CharacterTokenizer(' '));
        mSearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mSearchView.isPopupShowing()) {
                    mSearchView.showDropDown();
                }
            }
        });
        mSearchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mSearchView.setHint(R.string.search_text_hint_focused);
                    if (isResumed()) {
                        mSearchView.showDropDown();
                    }
                } else {
                    mSearchView.setHint(R.string.search_text_hint);
                    if (TextUtils.getTrimmedLength(mSearchView.getText()) == 0) {
                        hideKeyboard();
                    }
                }
            }
        });

        String search = getState().getString(STATE_QUERY);
        if (search != null) {
            mSearchView.setVisibility(View.VISIBLE);
            mSearchView.setText(search);
        }
    }

    private void requestSearch() {
        String query = getState().getString(STATE_QUERY);
        mCardManager.requestQuery(
                TextUtils.join(",", SearchUtils.toSearchTerms(query)));
    }

    private void hideKeyboard() {
        if (mSearchView == null) {
            return;
        }

        if (getView() != null) {
            getView().requestFocus();
        }
        mInputMethodManager.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
    }

    private boolean isShowingSecrets() {
        return CardManager.QUERY_SECRETS.equals(getState().getString(STATE_QUERY));
    }

}
