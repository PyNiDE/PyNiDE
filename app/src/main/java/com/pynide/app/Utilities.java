package com.pynide.app;

import android.graphics.Bitmap;

public class Utilities {
    public static native void stackBlurBitmap(Bitmap bitmap, int radius);
}
