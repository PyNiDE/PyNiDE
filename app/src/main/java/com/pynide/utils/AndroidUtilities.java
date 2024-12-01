package com.pynide.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.InsetDrawable;
import android.os.Environment;
import android.view.Menu;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.Utils;

import java.io.File;

public class AndroidUtilities {
    public static void setOptionalMenuIcons(Menu menu, boolean visible) {
        if (menu == null) return;
        if (menu instanceof MenuBuilder) setOptionalMenuIcons((MenuBuilder) menu, visible);
    }

    public static void setOptionalMenuIcons(MenuBuilder menuBuilder, boolean visible) {
        menuBuilder.setOptionalIconsVisible(visible);
        for (MenuItemImpl item : menuBuilder.getVisibleItems()) {
            if (item.getIcon() != null && item.requiresOverflow()) {
                item.setIcon(new InsetDrawable(item.getIcon(), SizeUtils.dp2px(8f), 0, SizeUtils.dp2px(8f), 0));
            }
        }
    }

    public static boolean isNightMode(Configuration configuration) {
        if (configuration == null) return false;
        var nightModeFlags = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isNightMode(Context context) {
        if (context == null) return false;
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
