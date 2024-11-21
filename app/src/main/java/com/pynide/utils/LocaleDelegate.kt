package com.pynide.utils

import android.app.Activity
import android.content.res.Configuration
import android.text.TextUtils

import java.util.Locale

class LocaleDelegate {
    private var locale = Locale.getDefault()

    val isLocaleChanged: Boolean
        get() = defaultLocale != locale

    fun updateConfiguration(configuration: Configuration) {
        locale = defaultLocale
        configuration.setLocale(locale)
    }

    fun onCreate(activity: Activity) {
        val decorView = activity.window.decorView
        decorView.layoutDirection = TextUtils.getLayoutDirectionFromLocale(locale)
    }

    companion object {
        @JvmStatic
        var defaultLocale: Locale = Locale.getDefault()

        @JvmStatic
        var systemLocale: Locale = Locale.getDefault()
    }
}