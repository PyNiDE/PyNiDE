package com.pynide.terminal

import com.pynide.ui.terminal.TerminalActivity
import com.termux.SimpleTerminalSessionClient
import com.termux.terminal.TerminalSession

class TerminalSessionClientImpl(private val activity: TerminalActivity) :
    SimpleTerminalSessionClient() {

    override fun onTextChanged(changedSession: TerminalSession) {
        super.onTextChanged(changedSession)
        activity.terminalView.onScreenUpdated()
    }

    override fun onSessionFinished(finishedSession: TerminalSession) {
        val service = activity.terminalService
        if (service == null || service.isWantsToStop) {
            activity.finish()
            return
        }
        if ((finishedSession.exitStatus == 0 || finishedSession.exitStatus == 130)) {
            activity.finish()
        }
    }
}