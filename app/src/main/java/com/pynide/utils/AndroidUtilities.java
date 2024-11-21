package com.pynide.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Environment;

import com.blankj.utilcode.util.Utils;

import java.io.File;

public class AndroidUtilities {
    public static boolean isNightMode(Configuration configuration) {
        var nightModeFlags = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isNightMode(Context context) {
        var configuration = context.getResources().getConfiguration();
        return isNightMode(configuration);
    }

    public static File getLogsDir() {
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                File path = Utils.getApp().getExternalFilesDir(null);
                File dir = new File(path, "/logs");
                dir.mkdirs();
                return dir;
            }
        } catch (Exception ignored) {

        }
        try {
            File dir = new File(Utils.getApp().getCacheDir(), "/logs");
            dir.mkdirs();
            return dir;
        } catch (Exception ignored) {

        }
        try {
            File dir = new File(Utils.getApp().getFilesDir(), "/logs");
            dir.mkdirs();
            return dir;
        } catch (Exception ignored) {

        }
        return null;
    }
}
