package com.pynide.app

import android.content.res.Resources

import com.pynide.R

open class IDEActivity : MaterialActivity() {
    override fun computeUserThemeKey(): String {
        return ThemeSettings.getTheme(this) + ThemeSettings.isUsingDynamicColors()
    }

    override fun onApplyUserThemeResource(theme: Resources.Theme?, isDecorView: Boolean) {
        if (ThemeSettings.isUsingDynamicColors()) {
            theme?.applyStyle(R.style.ThemeOverlay_PyNiDE_DynamicColors, true)
        }
        theme?.applyStyle(ThemeSettings.getThemeStyleRes(this), true)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!super.onSupportNavigateUp()) {
            finish()
        }
        return true
    }
}