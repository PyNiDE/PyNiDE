package com.pynide.ui.settings

import android.os.Bundle
import android.view.MenuItem

import androidx.activity.enableEdgeToEdge
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

import com.pynide.R
import com.pynide.app.IDEActivity
import com.pynide.databinding.ActivitySettingsBinding

class SettingsActivity : IDEActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private lateinit var binding: ActivitySettingsBinding

    companion object {
        private const val TITLE_TAG = "settings_title"
    }

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
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.settings)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence(TITLE_TAG, title)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (!supportFragmentManager.popBackStackImmediate()) {
                finish()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("DEPRECATION")
    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment!!
        )
        fragment.arguments = args
        fragment.setTargetFragment(caller, 0)

        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_container, fragment)
            .addToBackStack(null)
            .commit()

        setTitle(pref.title);
        return true
    }
}