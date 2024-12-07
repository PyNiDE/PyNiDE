package com.pynide.ui.launch

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

import androidx.activity.enableEdgeToEdge
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import com.blankj.utilcode.util.ActivityUtils

import com.pynide.R
import com.pynide.app.IDEActivity
import com.pynide.databinding.ActivityLaunchBinding
import com.pynide.ui.settings.SettingsActivity
import com.pynide.ui.terminal.TerminalActivity
import com.pynide.utils.AndroidUtilities

@Suppress("CustomSplashScreen")
class LaunchActivity : IDEActivity() {
    private lateinit var binding: ActivityLaunchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setTitle(R.string.python_ide)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.launch, menu)
        AndroidUtilities.setOptionalIcons(menu, true)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_terminal -> {
                val extras = bundleOf(TerminalActivity.KEY_IS_TERMINAL to true)
                ActivityUtils.startActivity(extras, this, TerminalActivity::class.java)
            }

            R.id.action_settings -> {
                ActivityUtils.startActivity(this, SettingsActivity::class.java)
            }

            R.id.action_interpreter -> {
                val extras = bundleOf(TerminalActivity.KEY_IS_TERMINAL to false)
                ActivityUtils.startActivity(extras, this, TerminalActivity::class.java)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
