package com.pynide.terminal;

import static com.pynide.editor.EditorHelper.ASSETS_FONT_PREFIX;

import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.Utils;

import com.pynide.IDESettings;
import com.pynide.R;
import com.pynide.model.AssetFile;
import com.pynide.utils.AndroidUtilities;
import com.pynide.utils.FileLog;

import com.termux.terminal.TerminalSession;
import com.termux.terminal.TerminalSessionClient;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class TerminalHelper {
    private static final String ASSETS_COLOR_SCHEME_PREFIX = "terminal/colors/";

    public static final String KEY_KEEP_SCREEN_ON = "terminal_keep_screen_on";
    public static final String KEY_FONT_SIZE = "terminal_font_size";
    public static final String KEY_FONT_STYLE = "terminal_font_style";
    public static final String KEY_COLOR_SCHEME = "terminal_color_scheme";

    public static final int DEFAULT_FONT_SIZE = 14;
    public static final String DEFAULT_FONT_STYLE = "reddit.ttf";
    public static final String DEFAULT_COLOR_SCHEME = "DYNAMIC";

    private static List<AssetFile> assetColorSchemes = null;

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
        return AndroidUtilities.getTypeface(ASSETS_FONT_PREFIX + getFontStyle());
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
    public static List<AssetFile> getAssetColorSchemes() {
        if (assetColorSchemes == null) {
            try {
                assetColorSchemes = Arrays.stream(Utils.getApp().getAssets().list(ASSETS_COLOR_SCHEME_PREFIX))
                        .filter(s -> s.endsWith(".properties"))
                        .map(AssetFile::new)
                        .sorted(Comparator.comparing(s -> s.displayName))
                        .collect(Collectors.toList());
                assetColorSchemes.add(0, new AssetFile(DEFAULT_COLOR_SCHEME, Utils.getApp().getString(R.string.color_scheme_dynamic)));
            } catch (IOException e) {
                throw new RuntimeException("Failed to load color-schemes from assets because " + e.getMessage());
            }
        }
        return assetColorSchemes;
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
        final var environmentVarsArray = AndroidUtilities.getEnvironmentVarsArray();
        final var commandArguments = AndroidUtilities.wrapCommandArguments(executablePath, argumentsArray, isLoginShell);

        final var newSession = new TerminalSession(commandArguments.first, workingDirectoryPath, commandArguments.second, environmentVarsArray, 4000, sessionClient);
        newSession.mSessionName = "Session";
        return newSession;
    }
}
