package com.pynide.terminal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SizeUtils;

import com.pynide.BuildVars;
import com.pynide.IDESettings;
import com.pynide.utils.FileLog;

import com.termux.terminal.TerminalSession;
import com.termux.terminal.TerminalSessionClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.collections.MapsKt;

public class TerminalHelper {
    public static final String DEFAULT_FONT_STYLE = "fonts/reddit_regular.ttf";
    public static final String DEFAULT_COLOR_SCHEME = "terminal/colors/material.properties";

    public static final String KEY_FONT_STYLE = "terminal_font_style";
    public static final String KEY_FONT_SIZE = "terminal_font_size";
    public static final String KEY_KEEP_SCREEN_ON = "terminal_keep_screen_on";

    public static TerminalSession createSession(@Nullable TerminalSessionClient sessionClient) {
        return createSession(null, null, null, sessionClient);
    }

    public static TerminalSession createSession(@Nullable File executable, @Nullable final File workingDirectory, @Nullable final List<String> arguments, @Nullable final TerminalSessionClient sessionClient) {
        final var isLoginShell = executable == null;

        if (executable == null) {
            final var shell = new File(TerminalVars.SHELL_PATH);
            if (FileUtils.isFileExists(shell)) {
                if (!shell.canExecute()) shell.setExecutable(true);
                executable = shell;
            }
        }

        final var executablePath = executable == null ? "/system/bin/sh" : executable.getAbsolutePath();
        final var workingDirectoryPath = workingDirectory == null ? TerminalVars.HOME_PATH : workingDirectory.getAbsolutePath();
        final var argumentsArray = arguments == null ? new String[0] : arguments.toArray(new String[0]);
        final var environmentVarsArray = createEnvironmentVarsArray();
        final var sessionCommand = createShellCommandArguments(executablePath, argumentsArray, isLoginShell);

        final var tempSessionClient = new TerminalSessionClient() {
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

        var newSession = new TerminalSession(sessionCommand.executablePath, workingDirectoryPath, sessionCommand.argumentsArray, environmentVarsArray, 4000, tempSessionClient);
        newSession.mSessionName = "Session";
        return newSession;
    }

    @NonNull
    public static SessionCommand createShellCommandArguments(@NonNull String executablePath, @NonNull String[] arguments, boolean isLoginShell) {
        final File executable = new File(executablePath);
        String interpreter = null;
        try (FileInputStream in = new FileInputStream(executable)) {
            byte[] buffer = new byte[256];
            int bytesRead = in.read(buffer);
            if (bytesRead > 4) {
                //noinspection StatementWithEmptyBody
                if (buffer[0] == 0x7F && buffer[1] == 'E' && buffer[2] == 'L' && buffer[3] == 'F') {
                    // Elf file, do nothing.
                } else if (buffer[0] == '#' && buffer[1] == '!') {
                    // Try to parse shebang.
                    StringBuilder builder = new StringBuilder();
                    for (int i = 2; i < bytesRead; i++) {
                        char c = (char) buffer[i];
                        if (c == ' ' || c == '\n') {
                            //noinspection StatementWithEmptyBody
                            if (builder.length() == 0) {
                                // Skip whitespace after shebang.
                            } else {
                                // End of shebang.
                                String shebangExecutable = builder.toString();
                                if (shebangExecutable.startsWith("/usr") || shebangExecutable.startsWith("/bin")) {
                                    String[] parts = shebangExecutable.split("/");
                                    String binary = parts[parts.length - 1];
                                    interpreter = TerminalVars.PREFIX_PATH + "/bin/" + binary;
                                } else if (shebangExecutable.startsWith(TerminalVars.FILES_PATH)) {
                                    interpreter = shebangExecutable;
                                }
                                break;
                            }
                        } else {
                            builder.append(c);
                        }
                    }
                } else {
                    // No shebang and no ELF, use standard shell.
                    interpreter = TerminalVars.SHELL_PATH;
                }
            }
        } catch (IOException e) {
            FileLog.e(e);
        }

        var elfFileToExecute = interpreter == null ? executablePath : interpreter;

        var actualArguments = new ArrayList<String>();
        var processName = (isLoginShell ? "-" : "") + executable.getName();
        actualArguments.add(processName);

        String actualFileToExecute;
        if (elfFileToExecute.startsWith(TerminalVars.FILES_PATH)) {
            actualFileToExecute = "/system/bin/linker" + (android.os.Process.is64Bit() ? "64" : "");
            actualArguments.add(elfFileToExecute);
        } else {
            actualFileToExecute = elfFileToExecute;
        }

        if (interpreter != null) {
            actualArguments.add(executable.getAbsolutePath());
        }
        Collections.addAll(actualArguments, arguments);
        return new SessionCommand(actualFileToExecute, actualArguments.toArray(new String[0]));
    }

    public static String[] createEnvironmentVarsArray() {
        var environmentVars = createEnvironmentVars();
        var environmentVarsList = new ArrayList<String>(environmentVars.size());
        environmentVars.forEach((key, value) -> environmentVarsList.add(key + "=" + value));
        Collections.sort(environmentVarsList);
        return environmentVarsList.toArray(new String[0]);
    }

    public static Map<String, String> createEnvironmentVars() {
        FileUtils.createOrExistsDir(TerminalVars.FILES_PATH);
        FileUtils.createOrExistsDir(TerminalVars.HOME_PATH);
        FileUtils.createOrExistsDir(TerminalVars.PREFIX_PATH);
        FileUtils.createOrExistsDir(TerminalVars.TEMP_PATH);

        var environmentVars = new HashMap<String, String>();
        environmentVars.put("FILES", TerminalVars.FILES_PATH);
        environmentVars.put("HOME", TerminalVars.HOME_PATH);
        environmentVars.put("PREFIX", TerminalVars.PREFIX_PATH);
        environmentVars.put("TMPDIR", TerminalVars.TEMP_PATH);
        environmentVars.put("TMP", TerminalVars.TEMP_PATH);
        environmentVars.put("TERM", "xterm-256color");
        environmentVars.put("COLORTERM", "truecolor");
        environmentVars.put("LANG", "en_US.UTF-8");
        environmentVars.put("SHELL", TerminalVars.SHELL_PATH);

        var temp = String.valueOf(BuildVars.DEBUG_VERSION);
        environmentVars.put("PYNIDE_DEBUG", temp);
        temp = String.format("%s (%s)", BuildVars.VERSION_NAME, BuildVars.VERSION_CODE);
        environmentVars.put("PYNIDE_VERSION", temp);
        environmentVars.put("PYNIDE_PACKAGE", BuildVars.PACKAGE_NAME);

        temp = System.getenv("PATH");
        environmentVars.put("PATH", String.format("%s/bin:%s", TerminalVars.PREFIX_PATH, temp));
        temp = System.getenv("LD_LIBRARY_PATH");
        if (temp == null) {
            environmentVars.put("LD_LIBRARY_PATH", String.format("%s/lib", TerminalVars.PREFIX_PATH));
        } else {
            environmentVars.put("LD_LIBRARY_PATH", String.format("%s/lib:%s", TerminalVars.PREFIX_PATH, temp));
        }
        temp = String.format("%s/lib/terminal-exec.so", TerminalVars.PREFIX_PATH);
        if (FileUtils.isFileExists(temp)) {
            environmentVars.put("LD_PRELOAD", temp);
        }
        return MapsKt.toSortedMap(environmentVars);
    }

    public static String getFontStyle() {
        return IDESettings.getPreferences().getString(KEY_FONT_STYLE, DEFAULT_FONT_STYLE);
    }

    public static int getFontSizePx() {
        return SizeUtils.sp2px(getFontSize());
    }

    public static int getFontSize() {
        return IDESettings.getPreferences().getInt(KEY_FONT_SIZE, 14);
    }

    public static boolean isKeepScreenOn() {
        return IDESettings.getPreferences().getBoolean(KEY_KEEP_SCREEN_ON, true);
    }
}
