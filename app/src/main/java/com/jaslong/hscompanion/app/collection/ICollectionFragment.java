package com.jaslong.hscompanion.app.collection;

import com.jaslong.hscompanion.model.Card;
import com.jaslong.hscompanion.model.HeroClass;

import java.util.List;

public interface ICollectionFragment {

    public interface Callback{
        void onCardClicked(Card card, int position);
        void onHeroClassChanged(HeroClass heroClass);
    }

    void swapList(List<Card> list);
    int getFirstVisiblePosition();
    void triggerHeroClassChanged();

}
