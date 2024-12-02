package com.pynide.ui.settings

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle

import androidx.activity.enableEdgeToEdge

import com.pynide.R
import com.pynide.app.IDEActivity
import com.pynide.databinding.ActivitySettingsBinding

class SettingsActivity : IDEActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragment()).commit()
        }
    }

    override fun onApplyUserThemeResource(theme: Resources.Theme?, isDecorView: Boolean) {
        super.onApplyUserThemeResource(theme, isDecorView)
        theme?.applyStyle(R.style.ThemeOverlay_PyNiDE_Preference, true)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}