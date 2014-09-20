package com.jaslong.hscompanion.app.deck.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.model.DeckCard;
import com.jaslong.util.android.app.BaseFragment;

import java.util.List;

public class DeckStatsFragment extends BaseFragment {

    private TextView mCount;
    private ManaCostBucketsBar mManaCostBucketsBar;
    private List<DeckCard> mList;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.deck_stats_fragment, container, false);
        mCount = (TextView) view.findViewById(R.id.stats_count);
        mManaCostBucketsBar = (ManaCostBucketsBar) view.findViewById(R.id.mana_cost_buckets_bar);
        if (mList != null) {
            update();
        }
        return view;
    }

    public void swapList(List<DeckCard> list) {
        mList = list;
        if (getView() != null) {
            update();
        }
    }

    private void update() {
        int count = 0;
        for (DeckCard deckCard : mList) {
            count += deckCard.getCount();
        }
        mCount.setText(String.valueOf(count));
        mManaCostBucketsBar.update(mList);
    }

}
