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
import com.pynide.terminal.TerminalVars
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
            R.id.action_terminal -> startTerminal()
            R.id.action_settings -> startSettings()
            R.id.action_interpreter -> startInterpreter()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startTerminal() {
        val extras = bundleOf(TerminalVars.KEY_TERMINAL_TYPE to TerminalVars.TERMINAL_TYPE_DEFAULT)
        ActivityUtils.startActivity(extras, this, TerminalActivity::class.java)
    }

    private fun startSettings() {
        ActivityUtils.startActivity(this, SettingsActivity::class.java)
    }

    private fun startInterpreter() {
        val extras =
            bundleOf(TerminalVars.KEY_TERMINAL_TYPE to TerminalVars.TERMINAL_TYPE_INTERPRETER)
        ActivityUtils.startActivity(extras, this, TerminalActivity::class.java)
    }
}
