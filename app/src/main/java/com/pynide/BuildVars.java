package com.pynide;

import com.pynide.utils.FileLog;

public class BuildVars {
    public static boolean DEBUG_VERSION = BuildConfig.DEBUG;
    public static String VERSION_NAME = BuildConfig.VERSION_NAME;
    public static int VERSION_CODE = BuildConfig.VERSION_CODE;
    public static String PACKAGE_NAME = BuildConfig.APPLICATION_ID;

    static {
        if (DEBUG_VERSION) {
            Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> FileLog.fatal(exception));
        }
    }
}
