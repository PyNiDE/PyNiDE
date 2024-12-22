package com.pynide.app

import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.LayoutInflaterCompat
import androidx.core.view.WindowCompat

import rikka.insets.WindowInsetsHelper
import rikka.layoutinflater.view.LayoutInflaterFactory

open class DynamicActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        LayoutInflaterCompat.setFactory2(
            layoutInflater, LayoutInflaterFactory(delegate).addOnViewCreatedListeners(
                WindowInsetsHelper.LISTENER, ToolbarTitleAlignmentFixed.LISTENER
            )
        )
        resetTitle()
        applyWindowFlags()
        super.onCreate(savedInstanceState)
    }

    private fun resetTitle() {
        var label = try {
            packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA).labelRes
        } catch (ignored: PackageManager.NameNotFoundException) {
            0
        }
        if (label == 0) {
            label = applicationInfo.labelRes
        }
        if (label != 0) {
            setTitle(label)
        }
    }

    private fun applyWindowFlags() {
        var typedArray: TypedArray
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        typedArray = obtainStyledAttributes(intArrayOf(android.R.attr.windowLightStatusBar))
        val windowLightStatusBar = typedArray.getBoolean(0, false)
        typedArray.recycle()
        windowInsetsController.isAppearanceLightStatusBars = windowLightStatusBar

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            typedArray = obtainStyledAttributes(intArrayOf(android.R.attr.windowLightNavigationBar))
            val windowLightNavigationBar = typedArray.getBoolean(0, false)
            typedArray.recycle()
            windowInsetsController.isAppearanceLightNavigationBars = windowLightNavigationBar
        }
    }
}