package com.jaslong.hscompanion.app.deck.detail;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.model.DeckCard;

import java.util.List;

public class ManaCostBucketsBar extends LinearLayout {

    private static final int BUCKETS = 8;
    private static final int MIN_BUCKET_HEIGHT = 10;

    private final ManaCostBucket[] mBuckets;

    public ManaCostBucketsBar(Context context) {
        this(context, null);
    }

    public ManaCostBucketsBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWeightSum(BUCKETS);
        mBuckets = new ManaCostBucket[BUCKETS];
        for (int i = 0; i < BUCKETS; ++i) {
            ManaCostBucket bucket = new ManaCostBucket(context);
            String manaCost = String.valueOf(i);
            if (i == BUCKETS - 1) {
                manaCost += '+';
            }
            bucket.setManaCost(manaCost);

            addView(bucket, new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            mBuckets[i] = bucket;
        }
    }

    public void update(List<DeckCard> deckCards) {
        int[] counts = new int[BUCKETS];
        for (DeckCard deckCard : deckCards) {
            counts[getBucket(deckCard.getCard().getManaCost())] += deckCard.getCount();
        }

        int maxCount = 0;
        for (int count : counts) {
            if (count > maxCount) {
                maxCount = count;
            }
        }

        for (int i = 0; i < counts.length; ++i) {
            mBuckets[i].setBar(counts[i], Math.max(maxCount, MIN_BUCKET_HEIGHT));
        }
    }

    private static int getBucket(int manaCost) {
        return manaCost < BUCKETS ? manaCost : BUCKETS - 1;
    }

    private static class ManaCostBucket extends RelativeLayout {

        public ManaCostBucket(Context context) {
            this(context, null);
        }

        public ManaCostBucket(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public ManaCostBucket(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            View.inflate(context, R.layout.mana_cost_bucket, this);
        }

        public void setManaCost(String manaCost) {
            ((TextView) findViewById(R.id.mana_cost)).setText(manaCost);
        }

        public void setBar(int count, int maxBucketHeight) {
            TextView countTextView = (TextView) findViewById(R.id.mana_cost_count);
            countTextView.setText(String.valueOf(count));

            View bar = findViewById(R.id.bar);
            View notBar = findViewById(R.id.not_bar);
            LinearLayout parent = (LinearLayout) bar.getParent();
            parent.setWeightSum(maxBucketHeight);
            ((LinearLayout.LayoutParams) bar.getLayoutParams()).weight = count;
            ((LinearLayout.LayoutParams) notBar.getLayoutParams()).weight = maxBucketHeight - count;
            parent.requestLayout();
        }

    }

}
