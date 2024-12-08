package com.pynide.ui.terminal

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent

import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible

import com.blankj.utilcode.util.ClipboardUtils

import com.google.android.material.color.MaterialColors

import com.pynide.R
import com.pynide.app.IDEActivity
import com.pynide.databinding.ActivityTerminalBinding
import com.pynide.terminal.BellHandler
import com.pynide.terminal.TerminalHelper
import com.pynide.utils.AndroidUtilities
import com.pynide.utils.Utilities

import com.termux.terminal.TerminalColors
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.terminal.TextStyle
import com.termux.view.TerminalViewClient

class TerminalActivity : IDEActivity(), TerminalViewClient, TerminalSessionClient {
    private lateinit var binding: ActivityTerminalBinding
    private val progressBar get() = binding.progressBar
    private val terminalRootView get() = binding.terminalRootView
    private val terminalView get() = binding.terminalView

    private val currentSession: TerminalSession? get() = terminalView.currentSession
    private val colorsProperties = TerminalHelper.getColorSchemeProperties()
    private lateinit var sessionType: TerminalSessionType
    private var isStarted: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            sessionType = intent.getParcelableExtra(
                KEY_TERMINAL_SESSION_TYPE, TerminalSessionType::class.java
            )!!
        } else {
            @Suppress("DEPRECATION")
            sessionType = intent.getParcelableExtra(KEY_TERMINAL_SESSION_TYPE)!!
        }

        binding = ActivityTerminalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        setTitle(
            when (sessionType) {
                TerminalSessionType.SHELL -> R.string.terminal
                TerminalSessionType.PYTHON -> R.string.interpreter
                else -> R.string.terminal
            }
        )

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (terminalView.isSelectingText) {
                    terminalView.stopTextSelectionMode()
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
        codePoint: Int, ctrlDown: Boolean, session: TerminalSession?
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
            BellHandler.getInstance().doBell()
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

    private fun onReset() {

    }

    private fun onShareTranscript() {

    }

    private fun onPaste() {

    }

    private fun setupTerminalView() {
        terminalView.setTextSize(TerminalHelper.getFontSizePx())
        terminalView.keepScreenOn = TerminalHelper.isKeepScreenOn()
        terminalView.setTerminalViewClient(this)
        terminalView.setTypeface(TerminalHelper.getTypeface())
        terminalView.attachSession(TerminalHelper.createSession(this))
        updateTerminalColors()
        terminalView.isVisible = true
        terminalView.requestFocus()
    }

    private fun updateTerminalColors() {
//        val decorView = window.decorView
//        val systemBackgroundColor =
//            MaterialColors.getColor(decorView, com.google.android.material.R.attr.colorSurface)
//        val systemForegroundColor =
//            MaterialColors.getColor(decorView, com.google.android.material.R.attr.colorOnSurface)
//
//        colorsProperties["background"] = Utilities.toHexStringColor(systemBackgroundColor)
//        colorsProperties["foreground"] = Utilities.toHexStringColor(systemForegroundColor)
//        colorsProperties["cursor"] = Utilities.toHexStringColor(systemForegroundColor)
//        repeat(15) {
//            colorsProperties["color$it"] = Utilities.toHexStringColor(systemForegroundColor)
//        }

        TerminalColors.COLOR_SCHEME.updateWith(colorsProperties)
        val emulator = currentSession?.emulator
        emulator?.mColors?.reset()
        updateBackgroundColors()
    }

    private fun updateBackgroundColors() {
        val emulator = currentSession?.emulator
        val backgroundColor: Int = if (emulator != null) {
            emulator.mColors.mCurrentColors[TextStyle.COLOR_INDEX_BACKGROUND]
        } else {
            val backgroundHexColor = colorsProperties["background"] as String
            Color.parseColor(backgroundHexColor)
        }
        val isLightColor = MaterialColors.isColorLight(backgroundColor)

        val decorView = window.decorView
        decorView.setBackgroundColor(backgroundColor)
        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor

        val insetsController = WindowCompat.getInsetsController(window, decorView)
        insetsController.isAppearanceLightNavigationBars = isLightColor
        insetsController.isAppearanceLightStatusBars = isLightColor
    }

    companion object {
        const val KEY_TERMINAL_SESSION_TYPE = "terminal_session_type"
    }
}