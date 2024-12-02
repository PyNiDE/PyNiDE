package com.pynide.terminal;

import com.blankj.utilcode.util.SizeUtils;

import com.pynide.IDESettings;

public class TerminalHelper {
    public static final String KEY_TEXT_SIZE = "terminal_text_size";
    public static final String KEY_KEEP_SCREEN_ON = "terminal_keep_screen_on";

    public static int getTextSize() {
        return IDESettings.getPreferences().getInt(KEY_TEXT_SIZE, SizeUtils.dp2px(14f));
    }

    public static boolean isKeepScreenOn() {
        return IDESettings.getPreferences().getBoolean(KEY_KEEP_SCREEN_ON, true);
    }
}
