package com.jaslong.hscompanion.expansion;

import android.content.Context;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.google.android.vending.expansion.downloader.Helpers;
import com.jaslong.hscompanion.util.Logger;
import com.jaslong.hscompanion.app.HearthstoneApplication;

import java.io.IOException;

public class ExpansionUtil {

    private static final Logger sLogger = Logger.create("ExpansionUtil");

    private static ExpansionMetadata METADATA = new ExpansionMetadata(
            3, 53609403L,
            0, 0L);

    public static ZipResourceFile getExpansionFile() {
        sLogger.i(String.format("Getting expansion files for main: %s patch: %s",
                METADATA.mainVersion, METADATA.patchVersion));
        ZipResourceFile expansionFile;
        try {
            expansionFile = APKExpansionSupport.getAPKExpansionZipFile(
                    HearthstoneApplication.getInstance(),
                    METADATA.mainVersion,
                    METADATA.patchVersion);
        } catch (IOException e) {
            throw new IllegalStateException("Could not access expansion file!", e);
        }

        if (expansionFile == null) {
            throw new IllegalStateException("Expansion file null!");
        }

        return expansionFile;
    }

    public static boolean hasExpansionFiles() {
        Context appContext = HearthstoneApplication.getInstance();

        // Check main expansion
        if (METADATA.mainVersion> 0) {
            String mainFileName =
                    Helpers.getExpansionAPKFileName(appContext, true, METADATA.mainVersion);
            if (!Helpers.doesFileExist(appContext, mainFileName, METADATA.mainSize, true)) {
                return false;
            }
        }

        // Check patch expansion
        if (METADATA.patchVersion > 0) {
            String patchFileName =
                    Helpers.getExpansionAPKFileName(appContext, false, METADATA.patchVersion);
            if (!Helpers.doesFileExist(appContext, patchFileName, METADATA.patchSize, true)) {
                return false;
            }
        }

        return true;
    }

    private ExpansionUtil() {
        throw new UnsupportedOperationException();
    }

}
