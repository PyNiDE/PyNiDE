package com.pynide.terminal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SizeUtils;

import com.pynide.BuildVars;
import com.pynide.IDESettings;

import com.termux.terminal.TerminalEmulator;
import com.termux.terminal.TerminalSession;
import com.termux.terminal.TerminalSessionClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.collections.MapsKt;

public class TerminalHelper {
    public static final String FONT_NAME = "fonts/reddit_regular.ttf";
    public static final String COLOR_NAME = "terminal/colors/material.properties";

    public static final String KEY_TEXT_SIZE = "terminal_text_size";
    public static final String KEY_KEEP_SCREEN_ON = "terminal_keep_screen_on";

    public static TerminalSession createSession(@NonNull String shell, @NonNull String workingDirectory, @Nullable List<String> arguments, @Nullable TerminalSessionClient sessionClient) {
        var tempArguments = arguments == null ? new String[0] : arguments.toArray(new String[0]);
        var tempSessionClient = new TerminalSessionClient() {
            @Override
            public void onTextChanged(@NonNull TerminalSession changedSession) {
                if (sessionClient != null) {
                    sessionClient.onTextChanged(changedSession);
                }
            }

            @Override
            public void onTitleChanged(@NonNull TerminalSession changedSession) {
                if (sessionClient != null) {
                    sessionClient.onTitleChanged(changedSession);
                }
            }

            @Override
            public void onSessionFinished(@NonNull TerminalSession finishedSession) {
                if (sessionClient != null) {
                    sessionClient.onSessionFinished(finishedSession);
                }
            }

            @Override
            public void onCopyTextToClipboard(@NonNull TerminalSession session, String text) {
                if (sessionClient != null) {
                    sessionClient.onCopyTextToClipboard(session, text);
                }
            }

            @Override
            public void onPasteTextFromClipboard(@Nullable TerminalSession session) {
                if (sessionClient != null) {
                    sessionClient.onPasteTextFromClipboard(session);
                }
            }

            @Override
            public void onBell(@NonNull TerminalSession session) {
                if (sessionClient != null) {
                    sessionClient.onBell(session);
                }
            }

            @Override
            public void onColorsChanged(@NonNull TerminalSession session) {
                if (sessionClient != null) {
                    sessionClient.onColorsChanged(session);
                }
            }

            @Override
            public void onTerminalCursorStateChange(boolean state) {
                if (sessionClient != null) {
                    sessionClient.onTerminalCursorStateChange(state);
                }
            }
        };

        var newSession = new TerminalSession(
                shell,
                workingDirectory,
                tempArguments,
                createEnvironmentVarsArray(),
                TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS,
                tempSessionClient
        );
        newSession.mSessionName = "Session";
        return newSession;
    }

    public static String[] createEnvironmentVarsArray() {
        var vars = createEnvironmentVars();
        var varsList = new ArrayList<String>(vars.size());
        vars.forEach((key, value) -> varsList.add(key + "=" + value));
        Collections.sort(varsList);
        return varsList.toArray(new String[0]);
    }

    public static Map<String, String> createEnvironmentVars() {
        FileUtils.createOrExistsDir(TerminalVars.HOME);
        FileUtils.createOrExistsDir(TerminalVars.PREFIX);
        FileUtils.createOrExistsDir(TerminalVars.TEMP);

        var vars = new HashMap<String, String>();
        vars.put("HOME", TerminalVars.HOME);
        vars.put("PREFIX", TerminalVars.PREFIX);
        vars.put("TMPDIR", TerminalVars.TEMP);
        vars.put("TMP", TerminalVars.TEMP);
        vars.put("TERM", "xterm-256color");
        vars.put("COLORTERM", "truecolor");
        vars.put("LANG", "en_US.UTF-8");
        vars.put("SHELL", TerminalVars.SHELL);

        var temp = String.valueOf(BuildVars.DEBUG_VERSION);
        vars.put("PYNIDE_DEBUG", temp);
        temp = String.format("%s (%s)", BuildVars.VERSION_NAME, BuildVars.VERSION_CODE);
        vars.put("PYNIDE_VERSION", temp);
        vars.put("PYNIDE_PACKAGE", BuildVars.PACKAGE_NAME);

        temp = System.getenv("PATH");
        vars.put("PATH", String.format("%s/bin:%s", TerminalVars.PREFIX, temp));
        temp = System.getenv("LD_LIBRARY_PATH");
        if (temp == null) {
            vars.put("LD_LIBRARY_PATH", String.format("%s/lib", TerminalVars.PREFIX));
        } else {
            vars.put("LD_LIBRARY_PATH", String.format("%s/lib:%s", TerminalVars.PREFIX, temp));
        }
        temp = String.format("%s/lib/terminal-exec.so", TerminalVars.PREFIX);
        if (FileUtils.isFileExists(temp)) {
            vars.put("LD_PRELOAD", temp);
        }
        return MapsKt.toSortedMap(vars);
    }

    public static int getTextSize() {
        return IDESettings.getPreferences().getInt(KEY_TEXT_SIZE, SizeUtils.sp2px(14f));
    }

    public static boolean isKeepScreenOn() {
        return IDESettings.getPreferences().getBoolean(KEY_KEEP_SCREEN_ON, true);
    }
}
