package com.pynide.app;

import android.content.Context;

public class NativeLoader {
    private final static String LIB_NAME = "pynide";
    private static volatile boolean nativeLoaded = false;

    public static synchronized void initNativeLibs(Context context) {
        if (nativeLoaded) {
            return;
        }
        try {
            System.loadLibrary(LIB_NAME);
            nativeLoaded = true;
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("loaded normal lib");
            }
        } catch (Error e) {
            FileLog.e(e);
        }
    }

    public static boolean loaded() {
        return nativeLoaded;
    }
}
