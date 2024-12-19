package com.pynide.terminal

import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.StringUtils

import com.pynide.ui.terminal.TerminalActivity

import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession

class TerminalSessionClient(private val activity: TerminalActivity) :
    com.termux.terminal.TerminalSessionClient {
    private val isVisible: Boolean get() = activity.isActivityVisible

    override fun onTextChanged(changedSession: TerminalSession) {
        if (isVisible && activity.currentSession == changedSession) {
            activity.terminalView.onScreenUpdated()
        }
    }

    override fun onTitleChanged(changedSession: TerminalSession) {

    }

    override fun onSessionFinished(finishedSession: TerminalSession) {
        val service = activity.terminalService
        if (service == null || service.isWantsToStop) {
            activity.finish()
            return
        }
        if ((finishedSession.exitStatus == 0 || finishedSession.exitStatus == 130)) {
            removeFinishedSession(finishedSession)
        }
    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String?) {
        if (isVisible && !StringUtils.isTrimEmpty(text)) {
            ClipboardUtils.copyText(text?.trim())
        }
    }

    override fun onPasteTextFromClipboard(session: TerminalSession?) {
        val emulator = activity.terminalView.mEmulator ?: return
        val text = ClipboardUtils.getText().toString()
        if (isVisible && !StringUtils.isTrimEmpty(text)) {
            emulator.paste(text)
        }
    }

    override fun onBell(session: TerminalSession) {
        if (isVisible) {
            BellHandler.getInstance().doBell()
        }
    }

    override fun onColorsChanged(session: TerminalSession) {

    }

    override fun onTerminalCursorStateChange(state: Boolean) {

    }

    override fun setTerminalShellPid(session: TerminalSession, pid: Int) {

    }

    override fun getTerminalCursorStyle(): Int {
        return TerminalEmulator.TERMINAL_CURSOR_STYLE_BLOCK
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

    fun removeFinishedSession(finishedSession: TerminalSession?) {
        val service = activity.terminalService ?: return
        var index = service.removeSession(finishedSession)
        val size = service.sessions.size
        if (size == 0) {
            activity.finish()
        } else {
            if (index >= size) index = size - 1
            val session = service.getSession(index)
            if (session != null) {
                activity.setCurrentSession(session)
            }
        }
    }
}