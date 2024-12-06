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
import android.view.Window

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
import com.pynide.terminal.BellHandler
import com.pynide.terminal.TerminalHelper
import com.pynide.terminal.TerminalVars
import com.pynide.utils.AndroidUtilities

import com.termux.terminal.TerminalColors
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.terminal.TextStyle
import com.termux.view.TerminalViewClient

import java.util.Properties

class TerminalActivity : IDEActivity(), TerminalViewClient, TerminalSessionClient {
    private lateinit var binding: ActivityTerminalBinding
    private val progressBar get() = binding.progressBar
    private val terminalRootView get() = binding.terminalRootView
    private val terminalView get() = binding.terminalView

    private var isTerminal: Boolean = true
    private var isStarted: Boolean = true
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

        ViewCompat.setOnApplyWindowInsetsListener(terminalRootView) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            AndroidUtilities.toggleActionBar(supportActionBar, !imeVisible)
            WindowInsetsCompat.CONSUMED
        }

        progressBar.isVisible = false
    }

    override fun onStart() {
        super.onStart()
        isStarted = true
        terminalView.onScreenUpdated()
    }

    override fun onStop() {
        super.onStop()
        isStarted = false
    }

    override fun onScale(scale: Float): Float {
        return 1.0f
    }

    override fun onSingleTapUp(e: MotionEvent?) {
        val emulator = currentSession?.emulator ?: return
        if (!emulator.isMouseTrackingActive && e?.isFromSource(InputDevice.SOURCE_MOUSE) == false) {
            AndroidUtilities.showIme(window, terminalView)
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
        if (isStarted) {
            terminalView.onScreenUpdated()
        }
    }

    override fun onTitleChanged(changedSession: TerminalSession) {

    }

    override fun onSessionFinished(finishedSession: TerminalSession) {

    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String?) {
        if (isStarted && !text?.trim().isNullOrEmpty()) {
            ClipboardUtils.copyText(text?.trim())
        }
    }

    override fun onPasteTextFromClipboard(session: TerminalSession?) {
        val text = ClipboardUtils.getText().toString().trim()
        if (isStarted && text.isNotEmpty() && terminalView.mEmulator != null) {
            terminalView.mEmulator.paste(text)
        }
    }

    override fun onBell(session: TerminalSession) {
        if (isStarted) {
            BellHandler.getInstance(this).doBell()
        }
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
        val newSession = TerminalHelper.createSession(
            TerminalVars.SHELL,
            TerminalVars.HOME,
            null,
            this
        )
        return newSession
    }

    private fun updateBackgroundColors() {
        val decorView = window.decorView
        val contentView = decorView.findViewById<View>(Window.ID_ANDROID_CONTENT)
        val emulator = currentSession?.emulator ?: return
        val backgroundColor = emulator.mColors.mCurrentColors[TextStyle.COLOR_INDEX_BACKGROUND]
        decorView.setBackgroundColor(backgroundColor)
        contentView.setBackgroundColor(backgroundColor)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun updateTerminalColors() {
        val colorsProperties = Properties()
        assets.open(TerminalHelper.COLOR_NAME).use {
            colorsProperties.load(it)
        }

        val decorView = window.decorView
        fun toHexColor(color: Int): String {
            return "#" + color.toHexString().substring(2)
        }

        val systemBackgroundColor =
            MaterialColors.getColor(decorView, com.google.android.material.R.attr.colorSurface)
        val systemForegroundColor =
            MaterialColors.getColor(decorView, com.google.android.material.R.attr.colorOnSurface)

        colorsProperties["background"] = toHexColor(systemBackgroundColor)
        colorsProperties["foreground"] = toHexColor(systemForegroundColor)
        colorsProperties["cursor"] = toHexColor(systemForegroundColor)
        repeat(15) {
            colorsProperties["color$it"] = toHexColor(systemForegroundColor)
        }

        TerminalColors.COLOR_SCHEME.updateWith(colorsProperties)
        val emulator = currentSession?.emulator
        emulator?.mColors?.reset()
        updateBackgroundColors()

        Typeface.createFromAsset(assets, TerminalHelper.FONT_NAME).also(terminalView::setTypeface)
    }

    private fun setupTerminalView() {
        terminalView.setTextSize(TerminalHelper.getTextSizePx())
        terminalView.keepScreenOn = TerminalHelper.isKeepScreenOn()
        terminalView.setTerminalViewClient(this)
        terminalView.attachSession(createSession())
        updateTerminalColors()
        terminalView.isVisible = true
        terminalView.requestFocus()
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