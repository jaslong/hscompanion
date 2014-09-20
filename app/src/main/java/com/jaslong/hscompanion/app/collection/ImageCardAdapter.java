package com.jaslong.hscompanion.app.collection;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.util.concurrent.FutureCallback;
import com.jaslong.hscompanion.util.Logger;
import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.util.ColorPicker;
import com.jaslong.hscompanion.card.BitmapLoader;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.HeroClass;
import com.jaslong.util.android.adapter.BaseListAdapter;
import com.jaslong.util.android.concurrent.HandlerExecutor;
import com.jaslong.util.android.view.group.GroupListAdapter;

import java.util.concurrent.Executor;

/**
 * Adapter that represents a card with its image.
 */
class ImageCardAdapter extends BaseListAdapter<Card> implements GroupListAdapter {

    public static class ViewHolder extends com.jaslong.util.android.view.ViewHolder<View> {
        public ImageView image;
        @Override
        protected void init(View view) {
            image = (ImageView) view.findViewById(R.id.image);
        }
    }

    private static final Logger sLogger = Logger.create("ImageCardAdapter");

    private static final int CARD_IMAGE_WIDTH = 428;

    private final BitmapLoader mBitmapLoader;
    private final Executor mMainExecutor;
    private final int mTotalWidth;
    private final int mSpacing;
    private final Bitmap mDefaultCardImage;
    private int mSampleSize = 1;

    public ImageCardAdapter(Context context, int totalWidth, int spacing) {
        super(context, R.layout.image_card_item);
        mBitmapLoader = BitmapLoader.getInstance();
        mMainExecutor = HandlerExecutor.forMainThread();
        mTotalWidth = totalWidth;
        mSpacing = spacing;
        mDefaultCardImage = BitmapFactory.decodeResource(
                context.getResources(),
                R.drawable.cardback);
    }

    @Override
    public Object getGroup(int position) {
        Card card = getTypedItem(position);
        if (card == null) {
            return null;
        }

        HeroClass cardClass = card.getHeroClass();
        return cardClass != null ? cardClass : HeroClass.ALL_CLASSES;
    }

    @Override
    public View getGroupView(Object group, View convertView, ViewGroup parent) {
        TextView view = (TextView) convertView;
        if (view == null) {
            view = (TextView)
                    LayoutInflater.from(mContext).inflate(R.layout.class_header, parent, false);
        }

        HeroClass cardClass = (HeroClass) group;
        view.setText(cardClass.toString());
        view.setBackgroundColor(ColorPicker.getColor(cardClass, mContext.getResources()));
        return view;
    }

    @Override
    protected void bindView(View view, Context context, final Card card, int position) {
        final Object cardId = card.getCardId();
        view.setTag(R.id.image_card_id_key, card.getCardId());

        final ViewHolder viewHolder = ViewHolder.of(ViewHolder.class, view);
        final Resources resources = context.getResources();
        final String imageUri = card.getImageUri();

        viewHolder.image.setImageBitmap(mDefaultCardImage);
        mBitmapLoader.loadBitmap(resources, imageUri, mSampleSize,
                new FutureCallback<Bitmap>() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        if (cardId.equals(
                                viewHolder.getView().getTag(R.id.image_card_id_key))) {
                            viewHolder.image.setImageBitmap(bitmap);
                        } else {
                            sLogger.ic("View has changed: " + card.getName());
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        sLogger.w(t);
                    }
                }, mMainExecutor);
    }

    public void setNumColumns(int numColumns) {
        final int widthForImages = mTotalWidth - ((numColumns + 1) * mSpacing);
        final int targetWidth = widthForImages / numColumns;
        int currentWidth = CARD_IMAGE_WIDTH;
        int sampleSize = 1;
        while (true) {
            currentWidth /= 2;
            if (currentWidth > targetWidth) {
                sampleSize *= 2;
            } else {
                break;
            }
        }
        mSampleSize = sampleSize;
    }

}
