package com.pynide.app

import android.content.res.Resources
import android.os.Bundle

import androidx.core.app.ActivityCompat

import com.pynide.R

open class MaterialActivity : DynamicActivity() {
    private var userThemeKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
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

    override fun onResume() {
        super.onResume()
        if (userThemeKey != computeUserThemeKey()) {
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
}