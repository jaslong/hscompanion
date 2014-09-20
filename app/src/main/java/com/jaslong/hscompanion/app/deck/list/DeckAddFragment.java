package com.jaslong.hscompanion.app.deck.list;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.app.deck.detail.DeckNameFragment;
import com.jaslong.hscompanion.util.ColorPicker;
import com.jaslong.hscompanion.card.DeckManager;
import com.jaslong.hscompanion.card.Util;
import com.jaslong.hscompanion.database.DeckColumn;
import com.jaslong.hscompanion.model.HeroClass;
import com.jaslong.util.android.app.BaseDialogFragment;
import com.jaslong.util.android.app.CachedAsyncTaskLoader;

public class DeckAddFragment extends BaseDialogFragment implements
        LoaderManager.LoaderCallbacks<Long>,
        DeckNameFragment.Callback {

    public static final String TAG = "deck_add";

    public interface Callback {
        void onDeckAdded(long deckId);
    }

    private static final String STATE_CHOSEN_HERO_CLASS = "chosen_hero_class";

    private static final int COLUMN_COUNT = 3;

    private Callback mCallback;
    private DeckManager mDeckManager;
    private int mSpacing;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (Callback) getParentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDeckManager = new DeckManager(getActivity());
        mSpacing = getResources().getDimensionPixelOffset(R.dimen.grid_spacing);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.deck_add_fragment, null);
        final Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.deck_add_title)
                .setView(view)
                .create();

        LinearLayout grid = (LinearLayout) view.findViewById(R.id.grid);

        LinearLayout row = new LinearLayout(getActivity());
        row.setWeightSum(3.0f);
        int i = 0;
        for (final HeroClass heroClass : HeroClass.values()) {
            if (heroClass == HeroClass.ALL_CLASSES) {
                continue;
            }

            Button button = new Button(getActivity());
            button.setBackgroundColor(ColorPicker.getColor(heroClass, getResources()));
            button.setPadding(0, 0, 0, 0);
            button.setText(heroClass.toString());
            button.setTextColor(Color.WHITE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getState().putString(STATE_CHOSEN_HERO_CLASS, heroClass.toString());
                    DeckNameFragment.create("").show(
                            getChildFragmentManager(), DeckNameFragment.TAG);
                }
            });

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1.0f;
            if (++i != COLUMN_COUNT) {
                layoutParams.rightMargin = mSpacing;
                row.addView(button, layoutParams);
            } else {
                row.addView(button, layoutParams);
                grid.addView(row);

                row = new LinearLayout(getActivity());
                row.setPadding(0, mSpacing, 0, 0);
                i = 0;
            }
        }

        return dialog;
    }

    @Override
    public void onDeckName(String name) {
        Bundle bundle = new Bundle();
        bundle.putString(Util.nameOf(DeckColumn.NAME), name);
        bundle.putString(Util.nameOf(DeckColumn.HERO_CLASS),
                getState().getString(STATE_CHOSEN_HERO_CLASS));
        getLoaderManager().initLoader(0, bundle, this);
    }

    @Override
    public Loader<Long> onCreateLoader(int id, Bundle bundle) {
        final String name = bundle.getString(Util.nameOf(DeckColumn.NAME));
        final HeroClass heroClass = Util.toEnum(HeroClass.class,
                bundle.getString(Util.nameOf(DeckColumn.HERO_CLASS)));
        return new CachedAsyncTaskLoader<Long>(getActivity()) {
            @Override
            public Long loadInBackground() {
                return mDeckManager.addDeck(name, heroClass);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Long> loader, Long deckId) {
        if (deckId != null) {
            mCallback.onDeckAdded(deckId);
        } else {
            Toast.makeText(getActivity(), R.string.deck_add_error, Toast.LENGTH_LONG).show();
        }
        dismiss();
        getLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(Loader<Long> loader) {
    }

}
