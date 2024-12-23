package com.pynide

import android.app.Application

import androidx.appcompat.app.AppCompatDelegate

import com.blankj.utilcode.util.Utils

import com.pynide.utils.FileLog

class IDEApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
        NativeLoader.initNativeLibs()
        IDESettings.initialize(this)

        AppCompatDelegate.setDefaultNightMode(IDESettings.getNightMode())

        FileLog.d("app initied")
    }
}
