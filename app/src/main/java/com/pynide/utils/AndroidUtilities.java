package com.pynide.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.InsetDrawable;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import kotlin.collections.MapsKt;

public class AndroidUtilities {
    public static void toggleIme(@Nullable final Window window, @Nullable final View view, final boolean show) {
        if (window == null || view == null) return;
        final var insetsController = WindowCompat.getInsetsController(window, view);
        if (show) {
            view.requestFocus();
            insetsController.show(WindowInsetsCompat.Type.ime());
        } else {
            insetsController.hide(WindowInsetsCompat.Type.ime());
            view.clearFocus();
        }
    }

    public static void showIme(@Nullable final Window window, @Nullable final View view) {
        toggleIme(window, view, true);
    }

    public static void hideIme(@Nullable final Window window, @Nullable final View view) {
        toggleIme(window, view, false);
    }

    public static void toggleActionBar(@Nullable final ActionBar actionBar, final boolean show) {
        if (actionBar == null) return;
        if (show && !actionBar.isShowing()) {
            actionBar.show();
        } else if (!show && actionBar.isShowing()) {
            actionBar.hide();
        }
    }

    @SuppressLint("RestrictedApi")
    public static void setOptionalIcons(@NonNull final MenuBuilder menuBuilder, final boolean visible) {
        menuBuilder.setOptionalIconsVisible(visible);
        for (final var item : menuBuilder.getVisibleItems()) {
            if (item.getIcon() != null && item.requiresOverflow()) {
                item.setIcon(new InsetDrawable(item.getIcon(), SizeUtils.dp2px(8f), 0, SizeUtils.dp2px(8f), 0));
            }
        }
    }

    @SuppressLint("RestrictedApi")
    public static void setOptionalIcons(@Nullable final Menu menu, final boolean visible) {
        if (menu == null) return;
        if (menu instanceof MenuBuilder) setOptionalIcons((MenuBuilder) menu, visible);
    }

    public static boolean isNightMode(@Nullable final Configuration configuration) {
        if (configuration == null) return false;
        final var nightModeFlags = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isNightMode(@Nullable final Context context) {
        if (context == null) return false;
        final var configuration = context.getResources().getConfiguration();
        return isNightMode(configuration);
    }

    @NonNull
    public static Map<String, String> getFontNames() throws IOException {
        final var names = new HashMap<String, String>();
        final var namesArray = Arrays.stream(Utils.getApp().getAssets().list("fonts")).filter(s -> s.endsWith(".ttf"));
        namesArray.forEach(name -> {
            var temp = name.replace('-', ' ');
            final var dotIndex = temp.lastIndexOf('.');
            if (dotIndex != -1) temp = temp.substring(0, dotIndex);
            final var displayName = Utilities.capitalize(temp);
            names.put(name, displayName);
        });
        return MapsKt.toSortedMap(names);
    }

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
