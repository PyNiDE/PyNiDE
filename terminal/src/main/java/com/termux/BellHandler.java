package com.termux;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.blankj.utilcode.util.Utils;

public class BellHandler {
    private static final long DURATION = 50;
    private static final long MIN_PAUSE = 3 * DURATION;

    private static final Object lock = new Object();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastBell = 0;
    private final Runnable bellRunnable;

    private static volatile BellHandler instance = null;

    public static BellHandler getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new BellHandler();
                }
            }
        }
        return instance;
    }

    private BellHandler() {
        final Vibrator vibrator = Utils.getApp().getSystemService(Vibrator.class);

        bellRunnable = () -> {
            if (vibrator != null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(DURATION);
                    }
                } catch (Exception e) {
                    // Issue on samsung devices on android 8
                    // java.lang.NullPointerException: Attempt to read from field 'android.os.VibrationEffect com.android.server.VibratorService$Vibration.mEffect' on a null object reference
                }
            }
        };
    }

    public synchronized void doBell() {
        final long now = now();
        final long timeSinceLastBell = now - lastBell;

        //noinspection StatementWithEmptyBody
        if (timeSinceLastBell < 0) {
            // there is a next bell pending; don't schedule another one
        } else if (timeSinceLastBell < MIN_PAUSE) {
            // there was a bell recently, schedule the next one
            handler.postDelayed(bellRunnable, MIN_PAUSE - timeSinceLastBell);
            lastBell = lastBell + MIN_PAUSE;
        } else {
            // the last bell was long ago, do it now
            bellRunnable.run();
            lastBell = now;
        }
    }

    private long now() {
        return SystemClock.uptimeMillis();
    }
}