package com.pynide.app;

import android.content.res.Configuration;

import androidx.annotation.NonNull;

public class LocaleController {
    private static volatile LocaleController Instance = null;

    public static LocaleController getInstance() {
        LocaleController localInstance = Instance;
        if (localInstance == null) {
            synchronized (LocaleController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new LocaleController();
                }
            }
        }
        return localInstance;
    }

    public void onDeviceConfigurationChange(@NonNull Configuration newConfig) {

    }
}
