package com.pynide.app;

import android.content.Context;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.pynide.IDESettings;
import com.pynide.R;
import com.pynide.utils.AndroidUtilities;

public class ThemeHelper {
    private static final String THEME_DEFAULT = "DEFAULT";
    private static final String THEME_BLACK = "BLACK";

    public static final String KEY_LIGHT_THEME = "light_theme";
    public static final String KEY_BLACK_NIGHT_THEME = "black_night_theme";
    public static final String KEY_DYNAMIC_COLORS = "dynamic_colors";

    public static boolean isBlackNightTheme() {
        return IDESettings.getPreferences().getBoolean(KEY_BLACK_NIGHT_THEME, false);
    }

    public static boolean isDynamicColors() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && IDESettings.getPreferences().getBoolean(KEY_DYNAMIC_COLORS, true);
    }

    public static String getTheme(@Nullable final Context context) {
        if (isBlackNightTheme() && AndroidUtilities.isNightMode(context)) return THEME_BLACK;
        return IDESettings.getPreferences().getString(KEY_LIGHT_THEME, THEME_DEFAULT);
    }

    @StyleRes
    public static int getThemeStyleRes(@Nullable final Context context) {
        return switch (getTheme(context)) {
            case THEME_BLACK -> R.style.ThemeOverlay_PyNiDE_Black;
            default -> R.style.ThemeOverlay_PyNiDE_Normal;
        };
    }
}