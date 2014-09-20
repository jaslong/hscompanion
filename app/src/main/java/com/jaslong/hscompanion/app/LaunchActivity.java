package com.jaslong.hscompanion.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.jaslong.hscompanion.BuildConfig;

public class LaunchActivity extends Activity {

    private static final String PREFS_FILE = "LaunchActivity";
    private static final String KEY_INITIALIZED_VERSION = "initialized_version";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = HearthstoneApplication.getInstance()
                .getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        Intent intent;
        if (BuildConfig.VERSION_CODE == prefs.getInt(KEY_INITIALIZED_VERSION, 0)) {
            setInitialized();
            intent = new Intent(this, HearthstoneActivity.class);
        } else {
            intent = new Intent(this, InitializationActivity.class);
        }

        startActivity(intent);
        finish();
    }

    public static void setInitialized() {
        HearthstoneApplication.getInstance().getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
                .edit().putInt(KEY_INITIALIZED_VERSION, BuildConfig.VERSION_CODE).apply();
    }

}
