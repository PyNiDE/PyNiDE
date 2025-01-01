package com.pynide;

import androidx.annotation.NonNull;

import com.pynide.utils.FileLog;

import java.util.Arrays;

@SuppressWarnings("unused")
public class NativeLoader {
    private final static String[] LIB_NAMES = {"pynide", "terminal"};
    private static volatile boolean nativeLoaded = false;

    public static synchronized void initNativeLibs() {
        if (nativeLoaded) {
            return;
        }
        try {
            Arrays.stream(LIB_NAMES).forEach(System::loadLibrary);
            nativeLoaded = true;
            FileLog.d("loaded native libs");
        } catch (Error e) {
            FileLog.e(e);
        }
    }

    @NonNull
    public static String[] getLibNames() {
        return (String[]) Arrays.stream(LIB_NAMES).map(libName -> String.format("lib%s.so", libName)).toArray();
    }
}
