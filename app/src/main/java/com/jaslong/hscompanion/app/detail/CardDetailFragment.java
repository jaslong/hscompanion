package com.jaslong.hscompanion.app.detail;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.util.concurrent.FutureCallback;
import com.jaslong.hscompanion.util.Logger;
import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.app.HearthstoneActivity;
import com.jaslong.hscompanion.util.ColorPicker;
import com.jaslong.hscompanion.card.BitmapLoader;
import com.jaslong.hscompanion.card.CardManager;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.util.android.app.BaseFragment;
import com.jaslong.util.android.concurrent.HandlerExecutor;

import java.util.List;

public class CardDetailFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<List<Card>> {

    private static final Logger sLogger = Logger.create("CardDetailFragment");

    private static final String STATE_CARD_ID = "card_id";
    private static final String STATE_UPDATE_ACTIVITY = "update_activity";

    private boolean mLoaded = false;

    public static CardDetailFragment createInstance(String cardId) {
        return createInstance(cardId, true);
    }

    public static CardDetailFragment createInstance(String cardId, boolean updateActivity) {
        CardDetailFragment fragment = new CardDetailFragment();
        Bundle state = new Bundle();
        state.putString(STATE_CARD_ID, cardId);
        state.putBoolean(STATE_UPDATE_ACTIVITY, updateActivity);
        fragment.setArguments(state);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle state = savedInstanceState != null ? savedInstanceState : getArguments();
        if (state == null) {
            return;
        }

        CardManager cardManager = (CardManager) getLoaderManager().initLoader(0, null, this);
        cardManager.requestCard(getState().getString(STATE_CARD_ID));
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.detail_fragment, container, false);
    }

    @Override
    public Loader<List<Card>> onCreateLoader(int id, Bundle bundle) {
        return new CardManager(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Card>> loader, List<Card> cardList) {
        update(cardList.get(0));
    }

    @Override
    public void onLoaderReset(Loader<List<Card>> loader) {
    }

    private void update(final Card card) {
        if (getView() == null) {
            return;
        }

        int height = getView().getHeight();
        int width = getView().getWidth();
        if (height == 0 && width == 0) {
            getView().getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            try {
                                update(card);
                                return true;
                            } finally {
                                if (getView() != null) {
                                    getView().getViewTreeObserver().removeOnPreDrawListener(this);
                                }
                            }
                        }
                    });
            sLogger.dc("Waiting for view height and width.");
            return;
        }

        if (mLoaded) {
            return;
        }

        mLoaded = true;

        // Update activity views
        if (getState().getBoolean(STATE_UPDATE_ACTIVITY)) {
            HearthstoneActivity activity = (HearthstoneActivity) getActivity();
            activity.getSupportActionBar().setTitle(card.getName());
            activity.setTheme(card.getHeroClass());
        }

        // Set invisible view height
        View invisibleView = getView().findViewById(R.id.invisible_view);
        ImageView imageView = (ImageView) getView().findViewById(R.id.image);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                invisibleView.getLayoutParams());
        layoutParams.height =
                height - ((FrameLayout.LayoutParams) imageView.getLayoutParams()).bottomMargin;
        invisibleView.setLayoutParams(layoutParams);

        // Set info container color
        LinearLayout info = (LinearLayout) getView().findViewById(R.id.info_container);
        info.setBackgroundColor(ColorPicker.getColor(card.getHeroClass(), getResources()));

        // Set flavor text
        if (card.getFlavorText() != null) {
            ((TextView) info.findViewById(R.id.flavor_text)).setText(
                    Html.fromHtml(card.getFlavorText()));
        }

        // Add more info
        addInfo(info, R.string.card_set, card.getSet().toString());
        addInfo(info, R.string.artist_name, card.getArtistName());
        addInfo(info, R.string.how_to_get_this_card, card.getHowToGetThisCard());
        addInfo(info, R.string.how_to_get_this_gold_card, card.getHowToGetThisGoldCard());

        // Load image
        BitmapLoader.getInstance().loadBitmap(
                getResources(),
                card.getImageUri(),
                1,
                new FutureCallback<Bitmap>() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        View view = getView();
                        if (view != null) {
                            ((ImageView) view.findViewById(R.id.image)).setImageBitmap(bitmap);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Throwable t) {
                        sLogger.w(t);
                    }
                },
                HandlerExecutor.forCurrentThread());
    }

    private void addInfo(LinearLayout container, @StringRes int key, CharSequence value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }

        LinearLayout info = new LinearLayout(getActivity());
        info.setOrientation(LinearLayout.VERTICAL);
        info.addView(createBoldTextView(getString(key)));
        info.addView(createTextView(value));

        container.addView(info, createMatchParentWidthLayoutParams());
    }

    private TextView createTextView(CharSequence text) {
        TextView textView = new TextView(getActivity());
        textView.setText(text);
        textView.setTextAppearance(getActivity(), R.style.DetailText);
        return textView;
    }

    private TextView createBoldTextView(CharSequence text) {
        TextView textView = createTextView(text);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        return textView;
    }

    private LinearLayout.LayoutParams createMatchParentWidthLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = getResources().getDimensionPixelOffset(R.dimen.spacing);
        return params;
    }

}
