package com.pynide.app;

public class NativeLoader {
    private final static String LIB_NAME = "pynide";
    private static volatile boolean nativeLoaded = false;

    public static synchronized void initNativeLibs() {
        if (nativeLoaded) {
            return;
        }
        try {
            System.loadLibrary(LIB_NAME);
            nativeLoaded = true;
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("loaded lib");
            }
        } catch (Error e) {
            FileLog.e(e);
        }
    }
}
