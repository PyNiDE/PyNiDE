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
import android.view.View

import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible

import com.blankj.utilcode.util.ClipboardUtils

import com.google.android.material.color.MaterialColors

import com.pynide.R
import com.pynide.app.IDEActivity
import com.pynide.databinding.ActivityTerminalBinding
import com.pynide.terminal.TerminalHelper
import com.pynide.terminal.TerminalVars
import com.pynide.utils.AndroidUtilities

import com.termux.terminal.TerminalColors.COLOR_SCHEME
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.terminal.TextStyle
import com.termux.view.TerminalViewClient

import java.util.Properties

class TerminalActivity : IDEActivity(), TerminalViewClient, TerminalSessionClient {
    private lateinit var binding: ActivityTerminalBinding
    private val progressBar get() = binding.progressBar
    private val terminalView get() = binding.terminalView

    private var isTerminal: Boolean = true
    private val currentSession: TerminalSession? get() = terminalView.currentSession

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        isTerminal = intent.getBooleanExtra(KEY_IS_TERMINAL, true)

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

        ViewCompat.setOnApplyWindowInsetsListener(terminalView) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            AndroidUtilities.toggleActionBar(supportActionBar, !imeVisible)
            WindowInsetsCompat.CONSUMED
        }

        terminalView.attachSession(createSession())
        updateBackgroundColors()

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
            AndroidUtilities.toggleIme(window, terminalView, true)
        }
    }

    override fun shouldBackButtonBeMappedToEscape(): Boolean {
        return false
    }

    override fun shouldEnforceCharBasedInput(): Boolean {
        return true
    }

    override fun isTerminalViewSelected(): Boolean {
        return terminalView.hasFocus()
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
        if (text?.trim().isNullOrEmpty()) return
        ClipboardUtils.copyText(text?.trim())
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
        updateBackgroundColors()
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
            R.id.action_paste -> onPaste()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createSession(): TerminalSession {
        val newSession = TerminalSession(
            TerminalVars.SHELL,
            TerminalVars.HOME,
            arrayOf<String>(),
            TerminalHelper.createEnvironmentVarsArray(),
            TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS,
            this
        )
        newSession.mSessionName = "Session"
        return newSession
    }

    private fun updateBackgroundColors() {
        val decorView = window.decorView
        val emulatorColors = currentSession?.emulator?.mColors ?: return
        val backgroundColor = emulatorColors.mCurrentColors[TextStyle.COLOR_INDEX_BACKGROUND]
        decorView.setBackgroundColor(backgroundColor)
        decorView.findViewById<View>(android.R.id.content).setBackgroundColor(backgroundColor)
        binding.root.setBackgroundColor(backgroundColor)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun updateTerminalColors() {
        Properties().apply {
            assets.open(TerminalHelper.COLOR_NAME).use {
                load(it)
            }

            val decorView = window.decorView
            MaterialColors.getColor(
                decorView, com.google.android.material.R.attr.colorSurface
            ).also { color ->
                put("background", "#" + color.toHexString().substring(2))
            }

            MaterialColors.getColor(
                decorView, com.google.android.material.R.attr.colorOnSurface
            ).also { color ->
                put("foreground", "#" + color.toHexString().substring(2))
            }

            MaterialColors.getColor(
                decorView, com.google.android.material.R.attr.colorOnSurface
            ).also { color ->
                put("cursor", "#" + color.toHexString().substring(2))
            }

            MaterialColors.getColor(
                decorView, com.google.android.material.R.attr.colorOnSurface
            ).also { color ->
                repeat(15) {
                    put("color$it", "#" + color.toHexString().substring(2))
                }
            }
        }.also(COLOR_SCHEME::updateWith)

        val emulatorColors = currentSession?.emulator?.mColors ?: return
        emulatorColors.reset()
        updateBackgroundColors()
    }

    private fun setupTerminalView() {
        terminalView.setTextSize(TerminalHelper.getTextSize())
        terminalView.keepScreenOn = TerminalHelper.isKeepScreenOn()
        terminalView.setTerminalViewClient(this)
        Typeface.createFromAsset(assets, TerminalHelper.FONT_NAME).also(terminalView::setTypeface)
        updateTerminalColors()
        terminalView.isVisible = true
    }

    private fun onReset() {

    }

    private fun onShareTranscript() {

    }

    private fun onPaste() {

    }

    companion object {
        private const val KEY_IS_TERMINAL = "is_terminal"

        fun newIntent(context: Context, isTerminal: Boolean): Intent {
            val intent = Intent(context, TerminalActivity::class.java).apply {
                putExtra(KEY_IS_TERMINAL, isTerminal)
            }
            return intent
        }
    }
}