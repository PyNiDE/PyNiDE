package com.pynide.ui;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;

import com.pynide.R;

@SuppressLint("CustomSplashScreen")
public class LaunchActivity extends BasePermissionsActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTheme(R.style.Theme_PyNiDE);
        try {
            setTaskDescription(new ActivityManager.TaskDescription(null, null, 0xffffffff));
        } catch (Throwable ignore) {

        }
        try {
            getWindow().setNavigationBarColor(0xff000000);
        } catch (Throwable ignore) {

        }
        getWindow().setBackgroundDrawableResource(R.drawable.transparent);
        super.onCreate(savedInstanceState);
    }
}
