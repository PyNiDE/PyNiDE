package com.pynide.ui.terminal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

import androidx.activity.enableEdgeToEdge
import androidx.core.view.isVisible

import com.pynide.R
import com.pynide.app.IDEActivity
import com.pynide.databinding.ActivityTerminalBinding
import com.pynide.utils.AndroidUtilities

class TerminalActivity : IDEActivity() {
    private lateinit var binding: ActivityTerminalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityTerminalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        with(supportActionBar) {
            this?.setDisplayHomeAsUpEnabled(true)
            this?.setHomeAsUpIndicator(R.drawable.ic_close)
        }

        if (intent.getBooleanExtra(KEY_TERMINAL, true)) {
            setTitle(R.string.terminal)
        } else {
            setTitle(R.string.interpreter)
        }

        binding.progressBar.isVisible = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.terminal, menu)
        AndroidUtilities.setOptionalIcons(menu, true)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {}
            R.id.action_share_transcript -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggleActionBar(show: Boolean) {
        supportActionBar?.let { actionBar ->
            if (show && !actionBar.isShowing) {
                actionBar.show()
            } else if (!show && actionBar.isShowing) {
                actionBar.hide()
            }
        }
    }

    companion object {
        private const val KEY_TERMINAL = "terminal"

        fun newIntent(context: Context, terminal: Boolean): Intent {
            val intent = Intent(context, TerminalActivity::class.java).apply {
                putExtra(KEY_TERMINAL, terminal)
            }
            return intent
        }
    }
}