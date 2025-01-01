package com.pynide.terminal

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent

import com.pynide.ui.terminal.TerminalActivity
import com.pynide.utils.AndroidUtilities
import com.termux.SimpleTerminalViewClient

import com.termux.terminal.TerminalSession

class TerminalViewClientImpl(private val activity: TerminalActivity) : SimpleTerminalViewClient() {

    private var copyMode: Boolean = false

    val isCopyMode get() = copyMode

    override fun onSingleTapUp(e: MotionEvent?) {
        val session = activity.currentSession ?: return
        val emulator = session.emulator ?: return
        if (!emulator.isMouseTrackingActive && e?.isFromSource(InputDevice.SOURCE_MOUSE) == false) {
            AndroidUtilities.showIme(activity.window, activity.terminalView)
        }
    }

    override fun copyModeChanged(copyMode: Boolean) {
        if (this.copyMode != copyMode) {
            this.copyMode = copyMode
            activity.invalidateOptionsMenu()
            activity.terminalOnBackPressedCallback.isEnabled = copyMode
        }
    }

    override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && session?.isRunning == false) {
            activity.finish()
            return true
        }
        return false
    }

    override fun onKeyUp(keyCode: Int, e: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && activity.terminalView.mEmulator == null) {
            activity.finish()
            return true
        }
        return false
    }
}