package com.pynide.terminal;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SizeUtils;

import com.pynide.IDESettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TerminalHelper {
    public static final String FONT_NAME = "fonts/reddit_regular.ttf";
    public static final String COLOR_NAME = "terminal/colors/material.properties";

    public static final String KEY_TEXT_SIZE = "terminal_text_size";
    public static final String KEY_KEEP_SCREEN_ON = "terminal_keep_screen_on";

    public static String[] createEnvironmentVarsArray() {
        var vars = createEnvironmentVars();
        var varsList = new ArrayList<String>(vars.size());
        for (var key : vars.keySet()) {
            varsList.add(key + "=" + vars.get(key));
        }
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
        vars.put("LANG", "en_US.UTF-8");
        vars.put("SHELL", TerminalVars.SHELL);

        var temp = System.getenv("PATH");
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
        return vars;
    }

    public static int getTextSize() {
        return IDESettings.getPreferences().getInt(KEY_TEXT_SIZE, SizeUtils.sp2px(14f));
    }

    public static boolean isKeepScreenOn() {
        return IDESettings.getPreferences().getBoolean(KEY_KEEP_SCREEN_ON, true);
    }
}
