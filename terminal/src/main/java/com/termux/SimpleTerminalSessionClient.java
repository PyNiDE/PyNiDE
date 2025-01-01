package com.termux;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.StringUtils;

import com.termux.terminal.TerminalEmulator;
import com.termux.terminal.TerminalSession;
import com.termux.terminal.TerminalSessionClient;

public abstract class SimpleTerminalSessionClient implements TerminalSessionClient {
    @Override
    public void onTextChanged(@NonNull TerminalSession changedSession) {

    }

    @Override
    public void onTitleChanged(@NonNull TerminalSession changedSession) {

    }

    @Override
    public void onSessionFinished(@NonNull TerminalSession finishedSession) {

    }

    @Override
    public void onCopyTextToClipboard(@NonNull TerminalSession session, String text) {
        if (AppUtils.isAppForeground() && !StringUtils.isTrimEmpty(text)) {
            ClipboardUtils.copyText(text);
        }
    }

    @Override
    public void onPasteTextFromClipboard(@Nullable TerminalSession session) {

    }

    @Override
    public void onBell(@NonNull TerminalSession session) {
        if (AppUtils.isAppForeground()) {
            BellHandler.getInstance().doBell();
        }
    }

    @Override
    public void onColorsChanged(@NonNull TerminalSession session) {

    }

    @Override
    public void onTerminalCursorStateChange(boolean state) {

    }

    @Override
    public void setTerminalShellPid(@NonNull TerminalSession session, int pid) {

    }

    @Override
    public Integer getTerminalCursorStyle() {
        return TerminalEmulator.TERMINAL_CURSOR_STYLE_BLOCK;
    }

    @Override
    public void logError(String tag, String message) {

    }

    @Override
    public void logWarn(String tag, String message) {

    }

    @Override
    public void logInfo(String tag, String message) {

    }

    @Override
    public void logDebug(String tag, String message) {

    }

    @Override
    public void logVerbose(String tag, String message) {

    }

    @Override
    public void logStackTraceWithMessage(String tag, String message, Exception e) {

    }

    @Override
    public void logStackTrace(String tag, Exception e) {

    }
}
