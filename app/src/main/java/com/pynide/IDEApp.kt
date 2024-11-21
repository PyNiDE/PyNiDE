package com.pynide

import android.app.Application

import androidx.appcompat.app.AppCompatDelegate

import com.blankj.utilcode.util.Utils

import com.pynide.utils.FileLog
import com.pynide.utils.LocaleDelegate

class IDEApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
        NativeLoader.initNativeLibs()
        IDESettings.init(this)

        LocaleDelegate.defaultLocale = IDESettings.getLocale()
        AppCompatDelegate.setDefaultNightMode(IDESettings.getNightMode())

        FileLog.d("app initied");
    }
}
