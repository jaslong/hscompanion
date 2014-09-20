package com.jaslong.hscompanion.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Messenger;
import android.widget.TextView;

import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.DownloaderServiceMarshaller;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import com.google.android.vending.expansion.downloader.IDownloaderService;
import com.google.android.vending.expansion.downloader.IStub;
import com.jaslong.hscompanion.R;
import com.jaslong.hscompanion.database.HearthstoneHelper;
import com.jaslong.hscompanion.expansion.ExpansionDownloaderService;
import com.jaslong.hscompanion.expansion.ExpansionUtil;
import com.jaslong.hscompanion.util.Logger;

public class InitializationActivity extends Activity implements IDownloaderClient {

    private static final Logger sLogger = Logger.create("StartActivity");

    private boolean mIsResumed;
    private IStub mStub;
    private TextView mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActionBar() != null) {
            getActionBar().show();
        }
        setContentView(R.layout.initialization_activity);
        mMessage = (TextView) findViewById(R.id.message);

        checkExpansionFiles();
    }

    private void start() {
        LaunchActivity.setInitialized();
        startActivity(new Intent(this, HearthstoneActivity.class));
        finish();
    }

    private void setMessage(final CharSequence message) {
        sLogger.d(message.toString());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessage.setText(message);
            }
        });
    }

    private void checkDatabase() {
        sLogger.i("Checking database.");
        setMessage(getString(R.string.initialization_preparing));
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                new HearthstoneHelper(InitializationActivity.this).getReadableDatabase();
                if (mIsResumed) {
                    start();
                }
                return null;
            }
        }.execute();
    }

    private void checkExpansionFiles() {
        sLogger.i("Checking expansion files.");

        if (ExpansionUtil.hasExpansionFiles()) {
            checkDatabase();
            return;
        }

        Intent intent = new Intent(getIntent());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Start the download service (if required)
        int startResult = 0;
        try {
            startResult = DownloaderClientMarshaller.startDownloadServiceIfRequired(
                    this, pendingIntent, ExpansionDownloaderService.class);
        } catch (PackageManager.NameNotFoundException e) {
            sLogger.wtf(e);
        }

        if (startResult == DownloaderClientMarshaller.NO_DOWNLOAD_REQUIRED) {
            sLogger.i("No download required.");
            checkDatabase();
            return;
        }

        // Instantiate a member instance of IStub
        mStub = DownloaderClientMarshaller.CreateStub(this, ExpansionDownloaderService.class);

        // Set message to downloading at 0%.
        setMessage(String.format(getString(R.string.initialization_downloading), 0));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsResumed = true;
        if (mStub != null) {
            mStub.connect(this);
        }
    }

    @Override
    protected void onPause() {
        if (mStub != null) {
            mStub.disconnect(this);
        }
        mIsResumed = false;
        super.onPause();
    }



    @Override
    public void onServiceConnected(Messenger m) {
        IDownloaderService downloadService = DownloaderServiceMarshaller.CreateProxy(m);
        downloadService.onClientUpdated(mStub.getMessenger());
    }

    @Override
    public void onDownloadStateChanged(int newState) {
        String message = getString(Helpers.getDownloaderStringResourceIDFromState(newState));
        sLogger.d("onDownloadStateChanged: " + message);

        switch (newState) {
            case STATE_COMPLETED:
                if (!ExpansionUtil.hasExpansionFiles()) {
                    sLogger.e("Download completed but still don't have expansion files!");
                    finish();
                    return;
                }
                checkDatabase();
                break;
            case STATE_DOWNLOADING:
                sLogger.i("Starting download.");
                break;
            case STATE_FAILED_UNLICENSED:
            case STATE_FAILED_FETCHING_URL:
            case STATE_FAILED_SDCARD_FULL:
            case STATE_FAILED_CANCELED:
            case STATE_FAILED:
                sLogger.w("Download failed.");
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.initialization_failed))
                        .setMessage(getString(R.string.initialization_download_failed))
                        .setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .show();
                break;
            default:
                setMessage(message);
        }
    }

    @Override
    public void onDownloadProgress(DownloadProgressInfo progress) {
        long percentage = progress.mOverallProgress * 100 / progress.mOverallTotal;
        setMessage(String.format(getString(R.string.initialization_downloading), percentage));
    }

}
