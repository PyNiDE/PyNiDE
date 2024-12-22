package com.pynide.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
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

import org.telegram.messenger.FileLog;

import java.util.Hashtable;

public class AndroidUtilities {
    private static final Hashtable<String, Typeface> typefaceCache = new Hashtable<>();

    @Nullable
    public static Typeface getTypeface(@NonNull String assetPath) {
        synchronized (typefaceCache) {
            if (!typefaceCache.containsKey(assetPath)) {
                try {
                    Typeface typeface;
                    if (Build.VERSION.SDK_INT >= 26) {
                        Typeface.Builder builder = new Typeface.Builder(Utils.getApp().getAssets(), assetPath);
                        if (assetPath.contains("medium")) {
                            builder.setWeight(700);
                        }
                        if (assetPath.contains("italic")) {
                            builder.setItalic(true);
                        }
                        typeface = builder.build();
                    } else {
                        typeface = Typeface.createFromAsset(Utils.getApp().getAssets(), assetPath);
                    }
                    typefaceCache.put(assetPath, typeface);
                } catch (Exception e) {
                    FileLog.e("Could not get typeface '" + assetPath + "' because " + e.getMessage());
                    return null;
                }
            }
            return typefaceCache.get(assetPath);
        }
    }

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
    private static void setOptionalIcons(@NonNull final MenuBuilder menuBuilder, final boolean applyInsets) {
        if (!applyInsets) {
            menuBuilder.setOptionalIconsVisible(true);
            return;
        }
        for (final var item : menuBuilder.getVisibleItems()) {
            final var icon = item.getIcon();
            if (icon instanceof InsetDrawable) return;
            if (icon != null && item.requiresOverflow()) {
                final var iconPadding = SizeUtils.dp2px(8f);
                item.setIcon(new InsetDrawable(icon, iconPadding, 0, iconPadding, 0));
            }
        }
    }

    @SuppressLint("RestrictedApi")
    public static void setOptionalIcons(@Nullable final Menu menu, final boolean applyInsets) {
        if (menu == null) return;
        if (menu instanceof MenuBuilder) {
            setOptionalIcons((MenuBuilder) menu, applyInsets);
        }
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
}
