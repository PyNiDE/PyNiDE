package com.pynide.ui.settings

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
        with(supportActionBar) {
            this?.setDisplayShowHomeEnabled(true)
            this?.setDisplayHomeAsUpEnabled(true)
        }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }
}