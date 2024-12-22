package com.pynide.terminal;

import static com.pynide.utils.AndroidUtilities.ASSETS_FONT_PREFIX;

import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.blankj.utilcode.util.CleanUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.Utils;

import com.pynide.BuildVars;
import com.pynide.IDESettings;
import com.pynide.R;
import com.pynide.utils.AndroidUtilities;
import com.pynide.utils.Utilities;

import com.termux.terminal.TerminalSession;
import com.termux.terminal.TerminalSessionClient;

import org.telegram.messenger.FileLog;

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
    private static final String ASSETS_COLOR_SCHEME_PREFIX = "terminal/colors/";

    public static final String KEY_KEEP_SCREEN_ON = "terminal_keep_screen_on";
    public static final String KEY_FONT_SIZE = "terminal_font_size";
    public static final String KEY_FONT_STYLE = "terminal_font_style";
    public static final String KEY_COLOR_SCHEME = "terminal_color_scheme";

    public static final int DEFAULT_FONT_SIZE = 14;
    public static final String DEFAULT_FONT_STYLE = "reddit.ttf";
    public static final String DEFAULT_COLOR_SCHEME = "DYNAMIC";

    private static final Map<String, String> colorSchemesCache = new HashMap<>();

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

    @Nullable
    public static Typeface getFontStyleTypeface() {
        final var fontStyle = ASSETS_FONT_PREFIX + getFontStyle();
        return AndroidUtilities.getTypeface(fontStyle);
    }

    public static String getColorScheme() {
        return IDESettings.getPreferences().getString(KEY_COLOR_SCHEME, DEFAULT_COLOR_SCHEME);
    }

    @Nullable
    public static Properties getColorSchemeProperties() {
        if (getColorScheme().equals(DEFAULT_COLOR_SCHEME)) return null;
        final var colorScheme = ASSETS_COLOR_SCHEME_PREFIX + getColorScheme();
        try {
            final var properties = new Properties();
            try (final var in = Utils.getApp().getAssets().open(colorScheme)) {
                properties.load(in);
            }
            return properties;
        } catch (IOException e) {
            FileLog.e("Could not get colorScheme properties '" + colorScheme + "' because " + e.getMessage());
            return null;
        }
    }

    @NonNull
    public static Map<String, String> getColorSchemes() throws IOException {
        synchronized (colorSchemesCache) {
            if (colorSchemesCache.isEmpty()) {
                colorSchemesCache.put(Utils.getApp().getString(R.string.color_scheme_dynamic), DEFAULT_COLOR_SCHEME);

                final var tempArray = Arrays.stream(Utils.getApp().getAssets().list(ASSETS_COLOR_SCHEME_PREFIX)).filter(s -> s.endsWith(".properties")).sorted();
                tempArray.forEach(name -> {
                    var temp = name.replace('-', ' ');
                    final var dotIndex = temp.lastIndexOf('.');
                    if (dotIndex != -1) temp = temp.substring(0, dotIndex);
                    final var displayName = Utilities.capitalize(temp);
                    colorSchemesCache.put(displayName, name);
                });
            }
            return colorSchemesCache;
        }
    }

    @NonNull
    public static Map<String, String> createEnvironmentVars() {
        final var environmentVars = new HashMap<String, String>();
        var temp = String.format("%s/tmp", TerminalVars.PREFIX_PATH);

        FileUtils.createOrExistsDir(TerminalVars.FILES_PATH);
        FileUtils.createOrExistsDir(TerminalVars.HOME_PATH);
        FileUtils.createOrExistsDir(TerminalVars.PREFIX_PATH);
        FileUtils.createOrExistsDir(temp);

        environmentVars.put("HOME", TerminalVars.HOME_PATH);
        environmentVars.put("PREFIX", TerminalVars.PREFIX_PATH);
        environmentVars.put("TMPDIR", temp);
        environmentVars.put("TMP", temp);
        environmentVars.put("TERM", "xterm-256color");
        environmentVars.put("COLORTERM", "truecolor");
        environmentVars.put("LANG", "en_US.UTF-8");

        temp = String.format("%s/bin/sh", TerminalVars.PREFIX_PATH);
        if (FileUtils.isFileExists(temp)) {
            environmentVars.put("SHELL", temp);
        } else {
            environmentVars.put("SHELL", "/system/bin/sh");
        }

        temp = Utils.getApp().getString(R.string.app_name);
        environmentVars.put("IDE_NAME", temp);
        temp = String.valueOf(BuildVars.DEBUG_VERSION);
        environmentVars.put("IDE_DEBUG", temp);
        temp = String.format("%s (%s)", BuildVars.VERSION_NAME, BuildVars.VERSION_CODE);
        environmentVars.put("IDE_VERSION", temp);
        environmentVars.put("IDE_PACKAGE", BuildVars.PACKAGE_NAME);

        temp = System.getenv("PATH");
        temp = String.format("%s/bin:%s", TerminalVars.PREFIX_PATH, temp);
        environmentVars.put("PATH", temp);

        temp = System.getenv("LD_LIBRARY_PATH");
        if (temp == null) {
            temp = String.format("%s/lib", TerminalVars.PREFIX_PATH);
            environmentVars.put("LD_LIBRARY_PATH", temp);
        } else {
            temp = String.format("%s/lib:%s", TerminalVars.PREFIX_PATH, temp);
            environmentVars.put("LD_LIBRARY_PATH", temp);
        }

        temp = String.format("%s/lib/terminal-exec.so", TerminalVars.PREFIX_PATH);
        if (FileUtils.isFileExists(temp)) {
            environmentVars.put("LD_PRELOAD", temp);

            if (DeviceUtils.getSDKVersionCode() >= 29) {
                environmentVars.put("BASEDIR", TerminalVars.FILES_PATH);
                temp = String.valueOf(android.os.Process.is64Bit());
                environmentVars.put("PROC_64BIT", temp);
            }
        }
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
    public static Pair<String, String[]> createCommandArguments(@NonNull final String executablePath, @NonNull final String[] argumentsArray, final boolean isLoginShell) {
        String interpreter = null;

        final var executable = new File(executablePath);
        try (final var in = new FileInputStream(executable)) {
            final var buffer = new byte[256];
            final var bytesRead = in.read(buffer);
            if (bytesRead > 4) {
                //noinspection StatementWithEmptyBody
                if (buffer[0] == 0x7F && buffer[1] == 'E' && buffer[2] == 'L' && buffer[3] == 'F') {
                    // Elf file, do nothing.
                } else if (buffer[0] == '#' && buffer[1] == '!') {
                    // Try to parse shebang.
                    final var builder = new StringBuilder();
                    for (int i = 2; i < bytesRead; i++) {
                        final var c = (char) buffer[i];
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
        if (DeviceUtils.getSDKVersionCode() >= 29 && elfFileToExecute.startsWith(TerminalVars.FILES_PATH)) {
            actualFileToExecute = "/system/bin/linker" + (android.os.Process.is64Bit() ? "64" : "");
            actualArguments.add(elfFileToExecute);
        } else {
            actualFileToExecute = elfFileToExecute;
        }

        if (interpreter != null) {
            actualArguments.add(executablePath);
        }

        Collections.addAll(actualArguments, argumentsArray);
        return Pair.create(actualFileToExecute, actualArguments.toArray(new String[0]));
    }

    @NonNull
    public static Pair<File, List<String>> createCommandArguments(@NonNull final File executable, @NonNull final List<String> arguments, final boolean isLoginShell) {
        final var commandArguments = createCommandArguments(executable.getAbsolutePath(), arguments.toArray(new String[0]), isLoginShell);
        return Pair.create(new File(commandArguments.first), Arrays.asList(commandArguments.second));
    }

    @NonNull
    public static TerminalSession createSession(@Nullable File executable, @Nullable final File workingDirectory, @Nullable final List<String> arguments, @NonNull final TerminalSessionClient sessionClient) {
        final var isLoginShell = executable == null;

        if (executable == null) {
            final var shell = new File(TerminalVars.PREFIX_PATH, "bin/sh");
            if (FileUtils.isFileExists(shell)) {
                executable = shell;
            }
        }

        final var executablePath = executable == null ? "/system/bin/sh" : executable.getAbsolutePath();
        final var workingDirectoryPath = workingDirectory == null ? TerminalVars.HOME_PATH : workingDirectory.getAbsolutePath();
        final var argumentsArray = arguments == null ? new String[0] : arguments.toArray(new String[0]);
        final var environmentVarsArray = createEnvironmentVarsArray();
        final var commandArguments = createCommandArguments(executablePath, argumentsArray, isLoginShell);

        final var newSession = new TerminalSession(commandArguments.first, workingDirectoryPath, commandArguments.second, environmentVarsArray, 4000, sessionClient);
        newSession.mSessionName = "Session";
        return newSession;
    }

    public static void clearTMPDIR() {
        final var tmpDir = String.format("%s/tmp", TerminalVars.PREFIX_PATH);
        CleanUtils.cleanCustomDir(tmpDir);
        FileUtils.createOrExistsDir(tmpDir);
    }
}
