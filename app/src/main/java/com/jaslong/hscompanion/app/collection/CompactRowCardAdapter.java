package com.jaslong.hscompanion.app.collection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.util.ColorPicker;
import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.HeroClass;
import com.jaslong.util.android.adapter.BaseListAdapter;
import com.jaslong.util.android.view.group.GroupListAdapter;

/**
 * Adapter that represents a card with a compact row.
 */
class CompactRowCardAdapter extends BaseListAdapter<Card> implements GroupListAdapter {

    public static class ViewHolder extends com.jaslong.util.android.view.ViewHolder {
        public TextView manaCost;
        public TextView name;
        public TextView spell;
        public TextView attack;
        public TextView health;
        @Override
        protected void init(View view) {
            manaCost = (TextView) view.findViewById(R.id.mana_cost);
            name = (TextView) view.findViewById(R.id.name);
            spell = (TextView) view.findViewById(R.id.spell);
            attack = (TextView) view.findViewById(R.id.attack);
            health = (TextView) view.findViewById(R.id.health);
        }
    }

    public CompactRowCardAdapter(Context context) {
        super(context, R.layout.compact_row_card_item);
    }

    @Override
    protected void bindView(View view, Context context, Card card, int position) {
        ViewHolder viewHolder = ViewHolder.of(ViewHolder.class, view);
        viewHolder.manaCost.setText(String.valueOf(card.getManaCost()));
        viewHolder.name.setText(card.getName());
        viewHolder.name.setTextColor(
                ColorPicker.getColor(card.getRarity(), mContext.getResources()));
        switch (card.getType()) {
            case SPELL:
                viewHolder.spell.setVisibility(View.VISIBLE);
                viewHolder.spell.setText(card.isSecret() ? R.string.secret : R.string.spell);
                viewHolder.attack.setVisibility(View.INVISIBLE);
                viewHolder.health.setVisibility(View.INVISIBLE);
                break;
            case MINION:
                viewHolder.spell.setVisibility(View.INVISIBLE);
                viewHolder.attack.setVisibility(View.VISIBLE);
                viewHolder.attack.setText(String.valueOf(card.getAttack()));
                viewHolder.health.setVisibility(View.VISIBLE);
                viewHolder.health.setText(String.valueOf(card.getHealth()));
                viewHolder.health.setBackgroundResource(R.color.health);
                break;
            case WEAPON:
                viewHolder.spell.setVisibility(View.INVISIBLE);
                viewHolder.attack.setVisibility(View.VISIBLE);
                viewHolder.attack.setText(String.valueOf(card.getAttack()));
                viewHolder.health.setVisibility(View.VISIBLE);
                viewHolder.health.setText(String.valueOf(card.getDurability()));
                viewHolder.health.setBackgroundResource(R.color.weapon);
                break;
        }
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

}
