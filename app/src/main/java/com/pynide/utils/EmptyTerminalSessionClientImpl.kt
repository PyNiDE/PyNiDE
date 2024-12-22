package com.pynide.utils

import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient

class EmptyTerminalSessionClientImpl : TerminalSessionClient {
    override fun onTextChanged(changedSession: TerminalSession) {

    }

    override fun onTitleChanged(changedSession: TerminalSession) {

    }

    override fun onSessionFinished(finishedSession: TerminalSession) {

    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String?) {

    }

    override fun onPasteTextFromClipboard(session: TerminalSession?) {

    }

    override fun onBell(session: TerminalSession) {

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
}