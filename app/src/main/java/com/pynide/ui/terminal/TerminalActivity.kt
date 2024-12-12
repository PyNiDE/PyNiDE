package com.pynide.ui.terminal

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.view.InputDevice
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent

import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible

import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.IntentUtils

import com.pynide.R
import com.pynide.app.IDEActivity
import com.pynide.databinding.ActivityTerminalBinding
import com.pynide.terminal.BellHandler
import com.pynide.terminal.TerminalHelper
import com.pynide.terminal.TerminalService
import com.pynide.terminal.TerminalVars
import com.pynide.utils.AndroidUtilities

import com.termux.terminal.TerminalColors
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.terminal.TextStyle
import com.termux.view.TerminalViewClient

class TerminalActivity : IDEActivity(), TerminalViewClient, TerminalSessionClient,
    ServiceConnection {
    private lateinit var binding: ActivityTerminalBinding
    private val progressBar get() = binding.progressBar
    private val terminalRootView get() = binding.terminalRootView
    private val terminalView get() = binding.terminalView

    private val currentSession: TerminalSession? get() = terminalView.currentSession
    private var terminalType = TerminalVars.TERMINAL_TYPE_SHELL
    private var terminalService: TerminalService? = null
    private var isStarted: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        intent.getIntExtra(TerminalVars.KEY_TERMINAL_TYPE, TerminalVars.TERMINAL_TYPE_SHELL)
            .also { type ->
                terminalType = type
            }

        binding = ActivityTerminalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)

        setTitle(
            when (terminalType) {
                TerminalVars.TERMINAL_TYPE_SHELL -> R.string.terminal
                TerminalVars.TERMINAL_TYPE_PYTHON -> R.string.interpreter
                else -> R.string.terminal
            }
        )

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

        Intent(this, TerminalService::class.java).also { intent ->
            ContextCompat.startForegroundService(this, intent)
            if (!bindService(intent, this, BIND_AUTO_CREATE)) {
                throw RuntimeException("bindService() failed")
            }
        }
    }

    override fun onServiceConnected(componentName: ComponentName?, service: IBinder) {
        terminalService = (service as TerminalService.LocalBinder).terminalService

        val createdSession = terminalService!!.getOrCreateTerminalSession(terminalType)
        setCurrentSession(createdSession)
        progressBar.hide()

        terminalService!!.setTerminalSessionClient(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        finish()
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

    override fun onStart() {
        super.onStart()
        isStarted = true
        terminalService?.let { service ->
            setCurrentSession(service.getOrCreateTerminalSession(terminalType))
        }
        terminalView.onScreenUpdated()
    }

    override fun onStop() {
        isStarted = false
        super.onStop()
    }

    override fun onDestroy() {
        terminalService?.let {
            terminalService!!.unsetTerminalSessionClient()
            terminalService = null
        }
        try {
            unbindService(this)
        } catch (_: Throwable) {

        }
        super.onDestroy()
    }

    override fun onScale(scale: Float): Float {
        return 1.0f
    }

    override fun onSingleTapUp(e: MotionEvent?) {
        val session = currentSession ?: return
        val emulator = session.emulator ?: return
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
            removeFinishedSession(session)
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
        if (isStarted && currentSession == changedSession) {
            terminalView.onScreenUpdated()
        }
    }

    override fun onTitleChanged(changedSession: TerminalSession) {

    }

    override fun onSessionFinished(finishedSession: TerminalSession) {
        if (terminalService == null || terminalService?.wantsToStop == true) {
            finish()
            return
        }
        if ((finishedSession.exitStatus == 0 || finishedSession.exitStatus == 130)) {
            removeFinishedSession(finishedSession)
        }
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

    private fun onReset() {
        // TODO
    }

    private fun onShareTranscript() {
        val session = currentSession ?: return
        val emulator = session.emulator ?: return
        val buffer = emulator.screen ?: return
        val transcript = buffer.transcriptTextWithoutJoinedLines.trim()
        IntentUtils.getShareTextIntent(transcript).also { intent ->
            ActivityUtils.startActivity(this, intent)
        }
    }

    private fun onPaste() {
        val session = currentSession
        if (session == null || !session.isRunning) {
            return
        }
        val text = ClipboardUtils.getText().toString().trim()
        if (text.isNotEmpty() && session.emulator != null) {
            session.emulator.paste(text)
        }
    }

    private fun setupTerminalView() {
        terminalView.setTextSize(TerminalHelper.getFontSizePx())
        terminalView.keepScreenOn = TerminalHelper.isKeepScreenOn()
        terminalView.setTypeface(TerminalHelper.getTypeface())
        terminalView.setTerminalViewClient(this)
        updateBackgroundColors()
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
//        val colorsProperties = TerminalHelper.getColorSchemeProperties()
//        colorsProperties["background"] = ColorUtils.int2RgbString(systemBackgroundColor)
//        colorsProperties["foreground"] = ColorUtils.int2RgbString(systemForegroundColor)
//        colorsProperties["cursor"] = ColorUtils.int2RgbString(systemForegroundColor)
//        repeat(15) {
//            colorsProperties["color$it"] = ColorUtils.int2RgbString(systemForegroundColor)
//        }

        val colorsProperties = TerminalHelper.getColorSchemeProperties()
        TerminalColors.COLOR_SCHEME.updateWith(colorsProperties)
        val session = currentSession
        val emulator = session?.emulator
        emulator?.mColors?.reset()
        updateBackgroundColors()
    }

    private fun updateBackgroundColors() {
        if (isStarted) {
            val session = currentSession
            val emulator = session?.emulator
            val backgroundColor = if (emulator != null) {
                emulator.mColors.mCurrentColors[TextStyle.COLOR_INDEX_BACKGROUND]
            } else {
                val colorsProperties = TerminalHelper.getColorSchemeProperties()
                val backgroundHexColor = colorsProperties["background"] as String?
                if (backgroundHexColor != null) {
                    ColorUtils.string2Int(backgroundHexColor)
                } else Color.BLACK
            }
            terminalRootView.setBackgroundColor(backgroundColor)
        }
    }

    private fun setCurrentSession(session: TerminalSession) {
        terminalView.attachSession(session)
        updateTerminalColors()
    }

    private fun removeFinishedSession(finishedSession: TerminalSession) {

    }
}