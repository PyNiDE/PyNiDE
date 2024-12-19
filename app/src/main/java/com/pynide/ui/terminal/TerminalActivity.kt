package com.pynide.ui.terminal

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem

import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible

import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.IntentUtils

import com.pynide.R
import com.pynide.app.IDEActivity
import com.pynide.databinding.ActivityTerminalBinding
import com.pynide.terminal.TerminalHelper
import com.pynide.terminal.TerminalService
import com.pynide.terminal.TerminalSessionClient
import com.pynide.terminal.TerminalType
import com.pynide.terminal.TerminalVars
import com.pynide.terminal.TerminalViewClient
import com.pynide.utils.AndroidUtilities

import com.termux.terminal.TerminalColors
import com.termux.terminal.TerminalSession
import com.termux.terminal.TextStyle

class TerminalActivity : IDEActivity(), ServiceConnection {
    private lateinit var binding: ActivityTerminalBinding
    private val progressBar get() = binding.progressBar
    private val terminalRootView get() = binding.terminalRootView
    val terminalView get() = binding.terminalView
    private val terminalGroup get() = binding.terminalGroup

    val currentSession: TerminalSession? get() = terminalView.currentSession
    private var terminalType: TerminalType = TerminalVars.TERMINAL_TYPE_DEFAULT
    var terminalService: TerminalService? = null
    var isActivityVisible: Boolean = true
    private var terminalViewClient: TerminalViewClient? = null
    private var terminalSessionClient: TerminalSessionClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        handleIntent(intent)
        super.onCreate(savedInstanceState)

        binding = ActivityTerminalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        setTitle(terminalType.title)

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
        if (resources.getBoolean(R.bool.terminal_toggle_actionbar_when_soft_keyboard_change)) {
            ViewCompat.setOnApplyWindowInsetsListener(terminalRootView) { _, insets ->
                val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
                AndroidUtilities.toggleActionBar(supportActionBar, !imeVisible)
                WindowInsetsCompat.CONSUMED
            }
        }

        Intent(this, TerminalService::class.java).also { intent ->
            ContextCompat.startForegroundService(this, intent)
            if (!bindService(intent, this, BIND_AUTO_CREATE)) {
                throw RuntimeException("bindService() failed")
            }
        }
    }

    override fun onServiceConnected(componentName: ComponentName?, service: IBinder) {
        terminalService = (service as TerminalService.ServiceBinder).service
        terminalService!!.setSessionClient(terminalSessionClient!!)
        val createdSession = terminalService!!.createSession(null, null, null)
        setCurrentSession(createdSession)
        progressBar.hide()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.terminal, menu)
        AndroidUtilities.setOptionalIcons(menu, true)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.forEach { item ->
            if (item.itemId != R.id.action_paste) {
                item.setEnabled(terminalViewClient?.copyMode == false)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> onReset()
            R.id.action_share_transcript -> onShareTranscript()
            R.id.action_toggle_autoscroll -> onToggleAutoScroll()
            R.id.action_paste -> onPaste()
            R.id.action_select_url -> onSelectURL()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        isActivityVisible = true
        // TODO Set last selected session
        terminalView.onScreenUpdated()
    }

    override fun onStop() {
        isActivityVisible = false
        super.onStop()
    }

    override fun onDestroy() {
        if (terminalService != null) {
            terminalService!!.unsetSessionClient()
            terminalService = null
        }
        try {
            unbindService(this)
        } catch (_: Throwable) {

        }
        super.onDestroy()
    }

    private fun onReset() {

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

    private fun onToggleAutoScroll() {
        val session = currentSession ?: return
        val emulator = session.emulator ?: return
        emulator.toggleAutoScrollDisabled()
    }

    private fun onSelectURL() {

    }

    private fun onPaste() {
        val session = currentSession ?: return
        if (session.isRunning) {
            val text = ClipboardUtils.getText().toString().trim()
            if (text.isNotEmpty() && session.emulator != null) {
                session.emulator.paste(text)
            }
        }
    }

    private fun setupTerminalView() {
        terminalSessionClient = TerminalSessionClient(this)
        terminalViewClient = TerminalViewClient(this, terminalSessionClient!!)
        terminalView.setTextSize(TerminalHelper.getFontSizePx())
        terminalView.keepScreenOn = TerminalHelper.isKeepScreenOn()
        terminalView.setTypeface(TerminalHelper.getFontStyleTypeface())
        terminalView.setTerminalViewClient(terminalViewClient)
        updateTerminalColors()
        terminalGroup.isVisible = true
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
        val colors = session?.emulator?.mColors
        colors?.reset()

        val backgroundColor = if (colors != null) {
            colors.mCurrentColors[TextStyle.COLOR_INDEX_BACKGROUND]
        } else {
            val backgroundHexColor = colorsProperties["background"] as String?
            if (backgroundHexColor != null) {
                ColorUtils.string2Int(backgroundHexColor)
            } else Color.BLACK
        }
        terminalRootView.setBackgroundColor(backgroundColor)
    }

    fun setCurrentSession(session: TerminalSession?) {
        if (session == null) return
        if (terminalView.attachSession(session)) {
            updateTerminalColors()
        }
    }

    private fun handleIntent(intent: Intent?) {
        val temp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(TerminalVars.EXTRA_TERMINAL_TYPE, TerminalType::class.java)
        } else {
            @Suppress("DEPRECATION") intent?.getParcelableExtra(TerminalVars.EXTRA_TERMINAL_TYPE)
        }
        if (temp != null) terminalType = temp
    }
}