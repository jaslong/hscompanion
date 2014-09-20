package com.jaslong.util.android.app;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.lang.reflect.Field;

/**
 * Base dialog fragment that has the following features:
 * - Managed state
 * - Child fragment animation fix
 */
public class BaseDialogFragment extends DialogFragment {

    private static final String EXTRA_BUNDLE = "state_fragment_bundle";
    private static final int DEFAULT_ANIMATION_DURATION = 300;

    private Bundle mState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mState = savedInstanceState.getBundle(EXTRA_BUNDLE);
        } else if (getArguments() != null) {
            mState = getArguments();
        } else {
            mState = new Bundle();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(EXTRA_BUNDLE, mState);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        final Fragment parent = getParentFragment();
        // Apply the workaround only if this is a child fragment, and the parent
        // is being removed.
        if (!enter && parent != null && parent.isRemoving()) {
            // This is a workaround for the bug where child fragments disappear when
            // the parent is removed (as all children are first removed from the parent)
            // See https://code.google.com/p/android/issues/detail?id=55228
            Animation doNothingAnim = new AlphaAnimation(1, 1);
            doNothingAnim.setDuration(getNextAnimationDuration(parent, DEFAULT_ANIMATION_DURATION));
            return doNothingAnim;
        } else {
            return super.onCreateAnimation(transit, enter, nextAnim);
        }
    }

    /**
     * Returns the state.
     * This method can be called after {@link #onCreate(android.os.Bundle)}.
     * @return the state managed by {@link com.jaslong.util.android.app.BaseDialogFragment}.
     */
    protected Bundle getState() {
        if (mState == null) {
            throw new IllegalStateException("Cannot call getState() before onCreate!");
        }
        return mState;
    }

    private static long getNextAnimationDuration(Fragment fragment, long defValue) {
        try {
            // Attempt to get the resource ID of the next animation that
            // will be applied to the given fragment.
            Field nextAnimField = Fragment.class.getDeclaredField("mNextAnim");
            nextAnimField.setAccessible(true);
            int nextAnimResource = nextAnimField.getInt(fragment);
            Animation nextAnim =
                    AnimationUtils.loadAnimation(fragment.getActivity(), nextAnimResource);

            // ...and if it can be loaded, return that animation's duration
            return (nextAnim == null) ? defValue : nextAnim.getDuration();
        } catch (NoSuchFieldException | IllegalAccessException | Resources.NotFoundException e) {
            Log.w("StateFragment", e);
            return defValue;
        }
    }

}
