package com.pynide.terminal;

import android.annotation.SuppressLint;

import com.pynide.BuildVars;

@SuppressLint("SdCardPath")
public class TerminalVars {
    public static final String FILES_PATH = "/data/data/" + BuildVars.PACKAGE_NAME + "/files";
    public static final String HOME_PATH = FILES_PATH + "/home";
    public static final String PREFIX_PATH = FILES_PATH + "/usr";
    public static final String TEMP_PATH = PREFIX_PATH + "/tmp";
    public static final String SHELL_PATH = PREFIX_PATH + "/bin/sh";
}
