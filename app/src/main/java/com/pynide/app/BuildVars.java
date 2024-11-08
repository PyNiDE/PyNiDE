package com.pynide.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.pynide.BuildConfig;

@SuppressWarnings({"unused"})
public class BuildVars {
    public static boolean DEBUG_VERSION = BuildConfig.DEBUG;
    public static boolean LOGS_ENABLED = BuildConfig.DEBUG;
    public static String VERSION_NAME = BuildConfig.VERSION_NAME;
    public static int VERSION_CODE = BuildConfig.VERSION_CODE;

    static {
        if (ApplicationLoader.applicationContext != null) {
            SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", Context.MODE_PRIVATE);
            LOGS_ENABLED = DEBUG_VERSION || sharedPreferences.getBoolean("logsEnabled", DEBUG_VERSION);
            if (LOGS_ENABLED) {
                Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> FileLog.fatal(exception));
            }
        }
    }
}
