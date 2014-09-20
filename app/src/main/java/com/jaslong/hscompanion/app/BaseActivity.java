package com.jaslong.hscompanion.app;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.app.collection.CollectionControllerFragment;
import com.jaslong.hscompanion.app.deck.list.DeckListFragment;
import com.jaslong.hscompanion.card.CardManager;
import com.jaslong.hscompanion.model.HeroClass;
import com.jaslong.hscompanion.util.ColorPicker;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseActivity extends ActionBarActivity implements
        FragmentManager.OnBackStackChangedListener {

    private static final int[] MENU = {
            R.string.menu_cards,
            R.string.menu_decks,
            R.string.menu_secrets
    };

    private static final String TAG_BACKGROUND_COLOR = "background_color";
    private static final String TAG_BACKGROUND_COLOR_DARK = "background_color_dark";

    private Collection<WeakReference<Fragment>> mFragmentRefs = new LinkedList<>();

    private Menu mMenu;
    private DrawerLayout mDrawerLayout;
    private DrawerListener mDrawerListener;
    private boolean mIsHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hearthstone_activity);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListener = new DrawerListener(mDrawerLayout);
        mDrawerListener.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerListener);

        updateDrawerIndicator();

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        List<String> menuStrings = new ArrayList<>(MENU.length);
        for (int stringRes : MENU) {
            menuStrings.add(getString(stringRes));
        }
        ListView leftMenu = (ListView) mDrawerLayout.findViewById(R.id.left_menu);
        leftMenu.setOnItemClickListener(mDrawerListener);
        leftMenu.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        menuStrings));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerListener.syncState();
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        super.onDestroy();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        mFragmentRefs.add(new WeakReference<>(fragment));
    }

    @Override
    public void onBackPressed() {
        // Go to home collection view if we're not home and there's nothing left in the back stack.
        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.closeDrawer(Gravity.START);
        } else if (!mIsHome && getSupportFragmentManager().getBackStackEntryCount() == 0) {
            setCollection("");
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerListener.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackStackChanged() {
        updateDrawerIndicator();
    }

    private void updateDrawerIndicator() {
        mDrawerListener.setDrawerIndicatorEnabled(
                getSupportFragmentManager().getBackStackEntryCount() == 0);
    }

    private static Collection<View> getViewsByTag(ViewGroup root, String tag){
        List<View> views = new LinkedList<>();
        for (int i = 0; i < root.getChildCount(); ++i) {
            View child = root.getChildAt(i);
            if (tag.equals(child.getTag())) {
                views.add(child);
            }

            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }
        }
        return views;
    }

    public void setTheme(final HeroClass cardClass) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int color = ColorPicker.getColor(cardClass, getResources());
                int darkColor = Color.rgb(
                        Color.red(color) / 2,
                        Color.green(color) / 2,
                        Color.blue(color) / 2);

                // Set action bar color.
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(darkColor);
                }

                // Set background color for views with tag.
                Collection<View> views = getViewsByTag(
                        (ViewGroup) findViewById(android.R.id.content),
                        TAG_BACKGROUND_COLOR);
                for (View view : views) {
                    view.setBackgroundColor(color);
                }

                // Set dark background color for views with tag.
                views = getViewsByTag(
                        (ViewGroup) findViewById(android.R.id.content),
                        TAG_BACKGROUND_COLOR_DARK);
                for (View view : views) {
                    view.setBackgroundColor(darkColor);
                }
            }
        });
    }

    protected void setCollection(String query) {
        boolean isHome = "".equals(query);
        setContent(
                CollectionControllerFragment.createInstance(null, query),
                CollectionControllerFragment.TAG,
                isHome);
    }

    protected void setContent(Fragment fragment, String tag, boolean isHome) {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = fm.beginTransaction()
                .setCustomAnimations(R.anim.slide_right_enter, R.anim.slide_right_exit);
        for (WeakReference<Fragment> existingFragmentRef : mFragmentRefs) {
            Fragment existingFragment = existingFragmentRef.get();
            if (existingFragment != null) {
                transaction.remove(existingFragment);
            }
        }
        mIsHome = isHome;
        transaction
                .add(R.id.content_frame, fragment, tag)
                .commit();
    }

    private class DrawerListener extends ActionBarDrawerToggle
            implements DrawerLayout.DrawerListener, AdapterView.OnItemClickListener {

        private static final float GRAYED_OUT_ALPHA = 127f;

        private int mClickedPosition = -1;

        public DrawerListener(DrawerLayout drawerLayout) {
            super(BaseActivity.this, drawerLayout, R.string.open_drawer, R.string.close_drawer);
        }

        @Override
        public void onDrawerSlide(View view, float v) {
            for (int i = 0; i < mMenu.size(); ++i) {
                Drawable icon = mMenu.getItem(i).getIcon();
                if (icon != null) {
                    icon.setAlpha((int) ((GRAYED_OUT_ALPHA + 1) * (1f - v) + GRAYED_OUT_ALPHA));
                }
            }
        }

        @Override
        public void onDrawerOpened(View view) {
            for (int i = 0; i < mMenu.size(); ++i) {
                mMenu.getItem(i).setEnabled(false);
            }
        }

        @Override
        public void onDrawerClosed(View view) {
            for (int i = 0; i < mMenu.size(); ++i) {
                mMenu.getItem(i).setEnabled(true);
            }

            if (mClickedPosition != -1) {
                switch (MENU[mClickedPosition]) {
                    case R.string.menu_decks:
                        setContent(new DeckListFragment(), DeckListFragment.TAG, false);
                        break;
                    case R.string.menu_secrets:
                        setCollection(CardManager.QUERY_SECRETS);
                        break;
                    case R.string.menu_cards:
                    default:

                        setCollection("");
                        break;
                }
                mClickedPosition = -1;
            }
        }

        @Override
        public void onDrawerStateChanged(int i) {

        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mDrawerLayout.closeDrawers();
            mClickedPosition = position;
        }

    }

}
