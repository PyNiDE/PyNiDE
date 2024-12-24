package com.pynide.ui.settings

import android.content.res.Resources
import android.os.Bundle

import androidx.activity.enableEdgeToEdge

import com.blankj.utilcode.util.FragmentUtils

import com.pynide.R
import com.pynide.app.BaseActivity
import com.pynide.databinding.ActivitySettingsBinding

class SettingsActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            FragmentUtils.add(supportFragmentManager, SettingsFragment(), R.id.settings_container)
        }
    }

    override fun onApplyUserThemeResource(theme: Resources.Theme?, isDecorView: Boolean) {
        super.onApplyUserThemeResource(theme, isDecorView)
        theme?.applyStyle(R.style.ThemeOverlay_PyNiDE_Preference, true)
    }
}