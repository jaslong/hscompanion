package com.jaslong.hscompanion.app;

import android.os.Bundle;

public class HearthstoneActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Do not add fragments if this is the activity was re-created.
        if (savedInstanceState != null) {
            return;
        }

        setCollection("");
    }

}
