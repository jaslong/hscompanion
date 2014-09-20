package com.jaslong.util.android.view;

import android.view.View;

import com.jaslong.util.android.log.Logger;

/**
 * Holds a view's views.
 * <p>
 * Subclasses must be public and have a public empty constructor.
 */
public abstract class ViewHolder<V extends View> {

    private static final Logger LOG = new Logger("jUtil", "ViewHolder");

    private static Integer sKey;

    /**
     * Sets the key for the tag the {@link ViewHolder} will be associated with.
     * @param key key for the tag
     * @see View#setTag(int, Object)
     */
    public static void setKey(int key) {
        sKey = key;
    }

    public static <V extends View, T extends ViewHolder<V>> T of(Class<T> cls, V view) {
        Object o;
        if (sKey != null) {
            o = view.getTag(sKey);
        } else {
            o = view.getTag();
        }

        T viewHolder;
        if (cls.isInstance(o)) {
            viewHolder = cls.cast(o);
        } else {
            try {
                viewHolder = cls.newInstance();
            } catch (InstantiationException e) {
                LOG.e(cls.getName() + " must be a concrete class with an empty constructor.", e);
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                LOG.e(cls.getName() + " must have a public empty constructor.", e);
                throw new RuntimeException(e);
            }
            viewHolder.init(view);
            viewHolder.mView = view;
        }

        return viewHolder;
    }

    V mView;

    protected abstract void init(View view);

    public V getView() {
        return mView;
    }

}
