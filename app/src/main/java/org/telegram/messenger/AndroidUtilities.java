package org.telegram.messenger;

import android.os.Environment;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.Utils;

import java.io.File;

public class AndroidUtilities {
    @Nullable
    public static File getLogsDir() {
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                final var path = Utils.getApp().getExternalFilesDir(null);
                final var dir = new File(path, "logs");
                dir.mkdirs();
                return dir;
            }
        } catch (Exception ignored) {

        }
        try {
            final var dir = new File(Utils.getApp().getCacheDir(), "logs");
            dir.mkdirs();
            return dir;
        } catch (Exception ignored) {

        }
        try {
            final var dir = new File(Utils.getApp().getFilesDir(), "logs");
            dir.mkdirs();
            return dir;
        } catch (Exception ignored) {

        }
        return null;
    }
}
