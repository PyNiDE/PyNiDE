package com.pynide.app

import android.content.res.Resources

import com.pynide.R

open class BaseActivity : MaterialActivity() {
    override fun computeUserThemeKey(): String {
        return ThemeHelper.getTheme(this) + ThemeHelper.isDynamicColors()
    }

    override fun onApplyUserThemeResource(theme: Resources.Theme?, isDecorView: Boolean) {
        if (ThemeHelper.isDynamicColors()) {
            theme?.applyStyle(R.style.ThemeOverlay_PyNiDE_DynamicColors, true)
        }
        theme?.applyStyle(ThemeHelper.getThemeStyleRes(this), true)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!super.onSupportNavigateUp()) {
            finish()
        }
        return true
    }
}