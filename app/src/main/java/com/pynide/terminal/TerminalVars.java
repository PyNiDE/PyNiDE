package com.pynide.terminal;

import android.annotation.SuppressLint;

import com.pynide.BuildVars;

public class TerminalVars {
    @SuppressLint("SdCardPath")
    public static final String FILES_PATH = "/data/data/" + BuildVars.PACKAGE_NAME + "/files";
    public static final String HOME_PATH = FILES_PATH + "/home";
    public static final String PREFIX_PATH = FILES_PATH + "/usr";

    public static final String EXTRA_TERMINAL_TYPE = "terminal_type";
    public static final TerminalType TERMINAL_TYPE_DEFAULT = TerminalType.DEFAULT;
    public static final TerminalType TERMINAL_TYPE_INTERPRETER = TerminalType.INTERPRETER;
    public static final TerminalType TERMINAL_TYPE_CONSOLE = TerminalType.CONSOLE;
}
