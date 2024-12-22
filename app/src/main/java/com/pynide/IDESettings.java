package com.pynide;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.pynide.utils.EmptySharedPreferencesImpl;

public class IDESettings {
    public static final String NAME = "settings";
    public static final String NIGHT_MODE = "night_mode";

    private static SharedPreferences sPreferences;

    public static SharedPreferences getPreferences() {
        return sPreferences;
    }

    @NonNull
    private static Context getSettingsStorageContext(@NonNull final Context context) {
        var storageContext = context.createDeviceProtectedStorageContext();
        storageContext = new ContextWrapper(storageContext) {
            @Override
            public SharedPreferences getSharedPreferences(String name, int mode) {
                try {
                    return super.getSharedPreferences(name, mode);
                } catch (IllegalStateException e) {
                    return new EmptySharedPreferencesImpl();
                }
            }
        };
        return storageContext;
    }

    public static void initialize(@NonNull final Context context) {
        if (sPreferences == null) {
            sPreferences = getSettingsStorageContext(context)
                    .getSharedPreferences(NAME, Context.MODE_PRIVATE);
        }
    }

    @AppCompatDelegate.NightMode
    public static int getNightMode() {
        return getPreferences().getInt(NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}