package com.pynide.terminal

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent

import com.pynide.ui.terminal.TerminalActivity
import com.pynide.utils.AndroidUtilities

import com.termux.terminal.TerminalSession

class TerminalViewClient(
    private val activity: TerminalActivity,
    private val sessionClient: TerminalSessionClient?
) : com.termux.view.TerminalViewClient {
    var copyMode: Boolean = false

    override fun onScale(scale: Float): Float {
        return 1.0f
    }

    override fun onSingleTapUp(e: MotionEvent?) {
        val session = activity.currentSession ?: return
        val emulator = session.emulator ?: return
        if (!emulator.isMouseTrackingActive && e?.isFromSource(InputDevice.SOURCE_MOUSE) == false) {
            AndroidUtilities.showIme(activity.window, activity.terminalView)
        }
    }

    override fun shouldBackButtonBeMappedToEscape(): Boolean {
        return false
    }

    override fun shouldEnforceCharBasedInput(): Boolean {
        return true
    }

    override fun shouldUseCtrlSpaceWorkaround(): Boolean {
        return false
    }

    override fun isTerminalViewSelected(): Boolean {
        return true
    }

    override fun copyModeChanged(copyMode: Boolean) {
        if (this.copyMode != copyMode) {
            this.copyMode = copyMode
            activity.invalidateOptionsMenu()
        }
    }

    override fun onKeyDown(keyCode: Int, e: KeyEvent?, session: TerminalSession?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && session?.isRunning == false) {
            sessionClient?.removeFinishedSession(session)
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

    override fun onEmulatorSet() {

    }

    override fun logError(tag: String?, message: String?) {

    }

    override fun logWarn(tag: String?, message: String?) {

    }

    override fun logInfo(tag: String?, message: String?) {

    }

    override fun logDebug(tag: String?, message: String?) {

    }

    override fun logVerbose(tag: String?, message: String?) {

    }

    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {

    }

    override fun logStackTrace(tag: String?, e: Exception?) {

    }
}