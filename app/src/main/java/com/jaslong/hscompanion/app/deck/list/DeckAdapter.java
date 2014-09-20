package com.jaslong.hscompanion.app.deck.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.util.ColorPicker;
import com.jaslong.hscompanion.model.HeroClass;
import com.jaslong.hscompanion.model.Deck;
import com.jaslong.util.android.adapter.BaseListAdapter;
import com.jaslong.util.android.view.group.GroupListAdapter;

class DeckAdapter extends BaseListAdapter<Deck> implements GroupListAdapter {

    public DeckAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);
    }

    @Override
    protected void bindView(View view, Context context, Deck deck, int position) {
        ((TextView) view.findViewById(android.R.id.text1)).setText(deck.getName());
    }

    @Override
    public Object getGroup(int position) {
        Deck deck = getTypedItem(position);
        return deck != null ? deck.getDeckClass() : null;
    }

    @Override
    public View getGroupView(Object group, View convertView, ViewGroup parent) {
        TextView view = (TextView) convertView;
        if (view == null) {
            view = (TextView)
                    LayoutInflater.from(mContext).inflate(R.layout.class_header, parent, false);
        }

        HeroClass deckClass = (HeroClass) group;
        view.setText(deckClass.toString());
        view.setBackgroundColor(ColorPicker.getColor(deckClass, mContext.getResources()));
        return view;
    }

}
