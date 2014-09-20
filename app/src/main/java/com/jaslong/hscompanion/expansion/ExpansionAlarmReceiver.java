package com.jaslong.hscompanion.expansion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.jaslong.hscompanion.util.Logger;

public class ExpansionAlarmReceiver extends BroadcastReceiver {

    private static final Logger sLogger = Logger.create("Expansion", "ExpansionAlarmReceiver");

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            DownloaderClientMarshaller.startDownloadServiceIfRequired(context,
                    intent, ExpansionDownloaderService.class);
        } catch (PackageManager.NameNotFoundException e) {
            sLogger.e("Couldn't start download service.", e);
        }
    }

}
