package com.pynide.ui.terminal

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent

import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible

import com.blankj.utilcode.util.ClipboardUtils

import com.google.android.material.color.MaterialColors

import com.pynide.R
import com.pynide.app.IDEActivity
import com.pynide.databinding.ActivityTerminalBinding
import com.pynide.terminal.TerminalHelper
import com.pynide.utils.AndroidUtilities

import com.termux.terminal.TerminalColors
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalViewClient

class TerminalActivity : IDEActivity(), TerminalViewClient, TerminalSessionClient {
    private lateinit var binding: ActivityTerminalBinding
    private val progressBar get() = binding.progressBar
    private val terminalView get() = binding.terminalView

    private var isTerminal: Boolean = true
    private val currentSession: TerminalSession? get() = terminalView.currentSession

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        isTerminal = intent.getBooleanExtra(KEY_TERMINAL, true)

        binding = ActivityTerminalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        with(supportActionBar) {
            this?.setDisplayHomeAsUpEnabled(true)
            this?.setHomeAsUpIndicator(R.drawable.ic_close)
        }
        setTitle(if (isTerminal) R.string.terminal else R.string.interpreter)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (terminalView.isSelectingText) {
                    terminalView.stopTextSelectionMode()
                } else {
                    finish()
                }
            }
        })

        setupTerminalView()

        progressBar.isVisible = false
    }

    override fun onStart() {
        super.onStart()
        terminalView.onScreenUpdated()
    }

    override fun onScale(scale: Float): Float {
        return 1.0f
    }

    override fun onSingleTapUp(e: MotionEvent?) {
        val emulator = currentSession?.emulator ?: return
        if (!emulator.isMouseTrackingActive && e?.isFromSource(InputDevice.SOURCE_MOUSE) == false) {
            terminalView.requestFocus()
            WindowCompat.getInsetsController(window, terminalView)
                .show(WindowInsetsCompat.Type.ime())
        }
    }

    override fun shouldBackButtonBeMappedToEscape(): Boolean {
        return false
    }

    override fun shouldEnforceCharBasedInput(): Boolean {
        return true
    }

    override fun isTerminalViewSelected(): Boolean {
        return true
    }

    override fun copyModeChanged(copyMode: Boolean) {

    }

    override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && session?.isRunning == false) {
            finish()
            return true
        }
        return false
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && terminalView.mEmulator == null) {
            finish()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onLongPress(event: MotionEvent?): Boolean {
        return false
    }

    override fun readControlKey(): Boolean {
        return false
    }

    override fun readAltKey(): Boolean {
        return false
    }

    override fun readShiftKey(): Boolean {
        return false
    }

    override fun readFnKey(): Boolean {
        return false
    }

    override fun onCodePoint(
        codePoint: Int,
        ctrlDown: Boolean,
        session: TerminalSession?
    ): Boolean {
        return false
    }

    override fun onTextChanged(changedSession: TerminalSession) {
        if (currentSession == changedSession) {
            terminalView.onScreenUpdated()
        }
    }

    override fun onTitleChanged(changedSession: TerminalSession) {

    }

    override fun onSessionFinished(finishedSession: TerminalSession) {

    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String?) {
        val text2 = text?.trim()
        if (text2.isNullOrEmpty()) return
        ClipboardUtils.copyText(text2)
    }

    override fun onPasteTextFromClipboard(session: TerminalSession?) {
        val text = ClipboardUtils.getText().toString().trim()
        if (text.isNotEmpty() && terminalView.mEmulator != null) {
            terminalView.mEmulator.paste(text)
        }
    }

    override fun onBell(session: TerminalSession) {

    }

    override fun onColorsChanged(session: TerminalSession) {

    }

    override fun onTerminalCursorStateChange(state: Boolean) {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.terminal, menu)
        AndroidUtilities.setOptionalIcons(menu, true)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> onReset()
            R.id.action_share_transcript -> onShareTranscript()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createSession(): TerminalSession {
        val workingDirectory = filesDir.absolutePath
        val tempDirectory = cacheDir.absolutePath

        val environmentVars = arrayOfNulls<String>(2)
        environmentVars[0] = "HOME=$workingDirectory"
        environmentVars[1] = "TMPDIR=$tempDirectory"

        val newSession = TerminalSession(
            "/system/bin/sh",
            workingDirectory,
            arrayOf<String>(),
            environmentVars,
            TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS,
            this
        )
        newSession.mSessionName = "Session"
        return newSession
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun updateTerminalColors() {
        val decorView = window.decorView
        val backgroundColor =
            MaterialColors.getColor(decorView, com.google.android.material.R.attr.colorSurface)
        val foregroundColor =
            MaterialColors.getColor(decorView, com.google.android.material.R.attr.colorOnSurface)
        val cursorColor =
            MaterialColors.getColor(decorView, com.google.android.material.R.attr.colorOnSurface)
        val otherColor =
            MaterialColors.getColor(decorView, com.google.android.material.R.attr.colorOnSurface)

        val colorMap = mutableMapOf<Int, Int>()
        repeat(15) {
            colorMap[it] = otherColor
        }

        TerminalColors.COLOR_SCHEME.updateWith(
            "#" + foregroundColor.toHexString().substring(2),
            "#" + backgroundColor.toHexString().substring(2),
            "#" + cursorColor.toHexString().substring(2),
            colorMap.mapValues { "#" + it.value.toHexString().substring(2) }
        )

        val emulator = currentSession?.emulator ?: return
        emulator.mColors.reset()
    }

    private fun setupTerminalView() {
        terminalView.setTextSize(TerminalHelper.getTextSize())
        terminalView.keepScreenOn = TerminalHelper.isKeepScreenOn()
        terminalView.setTerminalViewClient(this@TerminalActivity)
        Typeface.createFromAsset(assets, TERMINAL_FONT_NAME).also(terminalView::setTypeface)
        updateTerminalColors()
        terminalView.attachSession(createSession())
        terminalView.isVisible = true
    }

    private fun onShareTranscript() {

    }

    private fun onReset() {

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
        private const val TERMINAL_FONT_NAME = "fonts/reddit_regular.ttf"

        fun newIntent(context: Context, isTerminal: Boolean): Intent {
            val intent = Intent(context, TerminalActivity::class.java).apply {
                putExtra(KEY_TERMINAL, isTerminal)
            }
            return intent
        }
    }
}