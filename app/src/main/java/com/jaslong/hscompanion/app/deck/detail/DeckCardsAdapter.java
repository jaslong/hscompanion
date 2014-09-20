package com.jaslong.hscompanion.app.deck.detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.util.ColorPicker;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.DeckCard;
import com.jaslong.hscompanion.model.HeroClass;
import com.jaslong.util.android.adapter.BaseListAdapter;
import com.jaslong.util.android.view.group.GroupListAdapter;

/**
 * Adapter that represents a card with a compact row.
 */
public class DeckCardsAdapter extends BaseListAdapter<DeckCard> implements GroupListAdapter {

    public static class ViewHolder extends com.jaslong.util.android.view.ViewHolder {
        public TextView count;
        public TextView manaCost;
        public TextView name;
        @Override
        protected void init(View view) {
            count = (TextView) view.findViewById(R.id.count);
            manaCost = (TextView) view.findViewById(R.id.mana_cost);
            name = (TextView) view.findViewById(R.id.name);
        }
    }

    public DeckCardsAdapter(Context context) {
        super(context, R.layout.deck_card_item);
    }

    @Override
    protected void bindView(View view, Context context, DeckCard deckCard, int position) {
        ViewHolder viewHolder = ViewHolder.of(ViewHolder.class, view);
        viewHolder.count.setText(deckCard.getCard().getRarity() == Card.Rarity.LEGENDARY ? "★" :
                String.valueOf(deckCard.getCount()));
        viewHolder.manaCost.setText(String.valueOf(deckCard.getCard().getManaCost()));
        viewHolder.name.setText(deckCard.getCard().getName());
        viewHolder.name.setTextColor(
                ColorPicker.getColor(deckCard.getCard().getRarity(), mContext.getResources()));
    }

    @Override
    public Object getGroup(int position) {
        DeckCard deckCard = getTypedItem(position);
        if (deckCard == null) {
            return null;
        }

        HeroClass cardClass = deckCard.getCard().getHeroClass();
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

}
