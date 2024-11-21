package com.pynide.ui.settings

import android.os.Bundle

import androidx.preference.PreferenceFragmentCompat

import com.pynide.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}