package com.pynide.app

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.LayoutInflaterCompat
import androidx.core.view.WindowCompat

import com.pynide.R
import com.pynide.utils.LocaleDelegate
import com.pynide.utils.ToolbarTitleAlignmentFix

import rikka.insets.WindowInsetsHelper
import rikka.layoutinflater.view.LayoutInflaterFactory

open class MaterialActivity : AppCompatActivity() {
    private val localeDelegate by lazy { LocaleDelegate() }
    private var userThemeKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        LayoutInflaterCompat.setFactory2(
            layoutInflater,
            LayoutInflaterFactory(delegate).addOnViewCreatedListeners(
                WindowInsetsHelper.LISTENER,
                ToolbarTitleAlignmentFix.LISTENER
            )
        )

        localeDelegate.onCreate(this)
        resetTitle()
        fixWindowFlags()

        userThemeKey = computeUserThemeKey()
        super.onCreate(savedInstanceState)
        onApplyUserThemeResourceForDecorView()
    }

    override fun onApplyThemeResource(theme: Resources.Theme?, resid: Int, first: Boolean) {
        if (parent == null) {
            theme?.applyStyle(resid, true)
        } else {
            try {
                theme?.setTo(parent.theme)
            } catch (_: Exception) {

            }
            theme?.applyStyle(resid, false)
        }
        onApplyUserThemeResource(theme, false)
        super.onApplyThemeResource(theme, R.style.ThemeOverlay_PyNiDE_Normal, first)
    }

    override fun attachBaseContext(newBase: Context) {
        val configuration = newBase.resources.configuration
        localeDelegate.updateConfiguration(configuration)
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onResume() {
        super.onResume()
        if (localeDelegate.isLocaleChanged || userThemeKey != computeUserThemeKey()) {
            ActivityCompat.recreate(this)
        }
    }

    private fun onApplyUserThemeResourceForDecorView() {
        val theme = window?.decorView?.context?.theme
        if (theme != null) {
            onApplyUserThemeResource(theme, true)
        }
    }

    open fun onApplyUserThemeResource(theme: Resources.Theme?, isDecorView: Boolean) {

    }

    open fun computeUserThemeKey(): String? {
        return null
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

    private fun fixWindowFlags() {
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