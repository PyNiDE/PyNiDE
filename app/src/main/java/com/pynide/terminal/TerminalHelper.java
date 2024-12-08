package com.pynide.terminal;

import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.Utils;

import com.pynide.BuildVars;
import com.pynide.IDESettings;
import com.pynide.utils.FileLog;
import com.pynide.utils.Utilities;

import com.termux.terminal.TerminalSession;
import com.termux.terminal.TerminalSessionClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kotlin.collections.MapsKt;

public class TerminalHelper {
    public static final String KEY_KEEP_SCREEN_ON = "terminal_keep_screen_on";
    public static final String KEY_FONT_SIZE = "terminal_font_size";
    public static final String KEY_FONT_STYLE = "terminal_font_style";
    public static final String KEY_COLOR_SCHEME = "terminal_color_scheme";

    public static final int DEFAULT_FONT_SIZE = 14;
    public static final String DEFAULT_FONT_STYLE = "reddit.ttf";
    public static final String DEFAULT_COLOR_SCHEME = "material.properties";

    public static boolean isKeepScreenOn() {
        return IDESettings.getPreferences().getBoolean(KEY_KEEP_SCREEN_ON, true);
    }

    public static int getFontSize() {
        return IDESettings.getPreferences().getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE);
    }

    public static int getFontSizePx() {
        return SizeUtils.sp2px(getFontSize());
    }

    public static String getFontStyle() {
        return IDESettings.getPreferences().getString(KEY_FONT_STYLE, DEFAULT_FONT_STYLE);
    }

    public static Typeface getTypeface() {
        final var fontStyle = String.format("fonts/%s", getFontStyle());
        return Typeface.createFromAsset(Utils.getApp().getAssets(), fontStyle);
    }

    public static String getColorScheme() {
        return IDESettings.getPreferences().getString(KEY_COLOR_SCHEME, DEFAULT_COLOR_SCHEME);
    }

    public static Properties getColorSchemeProperties() throws IOException {
        final var colorScheme = String.format("terminal/colors/%s", getColorScheme());
        final var properties = new Properties();
        try (final var in = Utils.getApp().getAssets().open(colorScheme)) {
            properties.load(in);
        }
        return properties;
    }

    @NonNull
    public static Map<String, String> getColorSchemeNames() throws IOException {
        final var names = new HashMap<String, String>();
        final var namesArray = Arrays.stream(Utils.getApp().getAssets().list("terminal/colors")).filter(s -> s.endsWith(".properties"));
        namesArray.forEach(name -> {
            var temp = name.replace('-', ' ');
            final var dotIndex = temp.lastIndexOf('.');
            if (dotIndex != -1) temp = temp.substring(0, dotIndex);
            final var displayName = Utilities.capitalize(temp);
            names.put(name, displayName);
        });
        return MapsKt.toSortedMap(names);
    }

    @NonNull
    public static Map<String, String> createEnvironmentVars() {
        final var environmentVars = new HashMap<String, String>();
        var temp = String.format("%s/tmp", TerminalVars.PREFIX_PATH);

        FileUtils.createOrExistsDir(TerminalVars.FILES_PATH);
        FileUtils.createOrExistsDir(TerminalVars.HOME_PATH);
        FileUtils.createOrExistsDir(TerminalVars.PREFIX_PATH);
        FileUtils.createOrExistsDir(temp);

        environmentVars.put("FILES", TerminalVars.FILES_PATH);
        environmentVars.put("HOME", TerminalVars.HOME_PATH);
        environmentVars.put("PREFIX", TerminalVars.PREFIX_PATH);
        environmentVars.put("TMPDIR", temp);
        environmentVars.put("TMP", temp);
        environmentVars.put("TERM", "xterm-256color");
        environmentVars.put("COLORTERM", "truecolor");
        environmentVars.put("LANG", "en_US.UTF-8");

        temp = String.format("%s/bin/sh", TerminalVars.PREFIX_PATH);
        environmentVars.put("SHELL", temp);

        temp = String.valueOf(BuildVars.DEBUG_VERSION);
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

        temp = String.valueOf(android.os.Process.is64Bit());
        environmentVars.put("PROC_64BIT", temp);
        return MapsKt.toSortedMap(environmentVars);
    }

    @NonNull
    public static String[] createEnvironmentVarsArray() {
        final var environmentVars = createEnvironmentVars();
        final var environmentVarsList = new ArrayList<String>(environmentVars.size());
        environmentVars.forEach((key, value) -> environmentVarsList.add(key + "=" + value));
        Collections.sort(environmentVarsList);
        return environmentVarsList.toArray(new String[0]);
    }

    @NonNull
    public static Pair<String, String[]> createShellCommandArguments(@NonNull final String executablePath, @NonNull final String[] arguments, final boolean isLoginShell) {
        final var executable = new File(executablePath);
        String interpreter = null;
        try (final var in = new FileInputStream(executable)) {
            final var buffer = new byte[256];
            final var bytesRead = in.read(buffer);
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
                                final var shebangExecutable = builder.toString();
                                if (shebangExecutable.startsWith("/usr") || shebangExecutable.startsWith("/bin")) {
                                    final var parts = shebangExecutable.split("/");
                                    final var binary = parts[parts.length - 1];
                                    interpreter = String.format("%s/bin/%s", TerminalVars.PREFIX_PATH, binary);
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
                    interpreter = String.format("%s/bin/sh", TerminalVars.PREFIX_PATH);
                }
            }
        } catch (IOException e) {
            FileLog.e(e);
        }

        final var elfFileToExecute = interpreter == null ? executablePath : interpreter;

        final var actualArguments = new ArrayList<String>();
        final var processName = (isLoginShell ? "-" : "") + executable.getName();
        actualArguments.add(processName);

        final String actualFileToExecute;
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
        return Pair.create(actualFileToExecute, actualArguments.toArray(new String[0]));
    }

    @NonNull
    public static TerminalSession createSession(@Nullable File executable, @Nullable final File workingDirectory, @Nullable final List<String> arguments, @Nullable final TerminalSessionClient sessionClient) {
        final var isLoginShell = executable == null;

        if (executable == null) {
            final var shell = new File(TerminalVars.PREFIX_PATH, "bin/sh");
            if (FileUtils.isFileExists(shell)) {
                if (!shell.canExecute()) shell.setExecutable(true);
                executable = shell;
            }
        }

        var executablePath = executable == null ? "/system/bin/sh" : executable.getAbsolutePath();
        final var workingDirectoryPath = workingDirectory == null ? TerminalVars.HOME_PATH : workingDirectory.getAbsolutePath();
        var argumentsArray = arguments == null ? new String[0] : arguments.toArray(new String[0]);
        final var environmentVarsArray = createEnvironmentVarsArray();

        final var sessionCommand = createShellCommandArguments(executablePath, argumentsArray, isLoginShell);
        executablePath = sessionCommand.first;
        argumentsArray = sessionCommand.second;

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

        final var newSession = new TerminalSession(executablePath, workingDirectoryPath, argumentsArray, environmentVarsArray, 4000, tempSessionClient);
        newSession.mSessionName = "Session";
        return newSession;
    }

    @NonNull
    public static TerminalSession createSession(@Nullable final TerminalSessionClient sessionClient) {
        return createSession(null, null, null, sessionClient);
    }
}
