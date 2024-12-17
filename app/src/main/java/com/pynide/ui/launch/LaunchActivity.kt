package com.pynide.ui.launch

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

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

    private val drawerOnBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            binding.drawerLayout.closeDrawers()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityLaunchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appbarLaunch.toolbar)
        setTitle(R.string.python_ide)

        setupDrawerLayout()
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
        val extras =
            bundleOf(TerminalVars.EXTRA_TERMINAL_TYPE to TerminalVars.TERMINAL_TYPE_DEFAULT)
        ActivityUtils.startActivity(extras, this, TerminalActivity::class.java)
    }

    private fun startSettings() {
        ActivityUtils.startActivity(this, SettingsActivity::class.java)
    }

    private fun startInterpreter() {
        val extras =
            bundleOf(TerminalVars.EXTRA_TERMINAL_TYPE to TerminalVars.TERMINAL_TYPE_INTERPRETER)
        ActivityUtils.startActivity(extras, this, TerminalActivity::class.java)
    }

    private fun setupDrawerLayout() {
        val drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.appbarLaunch.toolbar,
            R.string.show_files,
            R.string.hide_files
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                drawerOnBackPressedCallback.isEnabled = true
            }

            override fun onDrawerClosed(drawerView: View) {
                drawerOnBackPressedCallback.isEnabled = false
            }
        })

        binding.drawerLayout.post {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerOnBackPressedCallback.isEnabled = true
            }
        }
    }
}
