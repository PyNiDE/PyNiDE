package com.pynide.ui.terminal

import android.os.Bundle

import androidx.activity.enableEdgeToEdge
import androidx.core.view.isVisible

import com.pynide.app.IDEActivity
import com.pynide.databinding.ActivityTerminalBinding

class TerminalActivity : IDEActivity() {
    private lateinit var binding: ActivityTerminalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityTerminalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        with(supportActionBar) {
            this?.setDisplayShowHomeEnabled(true)
            this?.setDisplayHomeAsUpEnabled(true)
        }

        binding.progressBar.isVisible = true
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
}