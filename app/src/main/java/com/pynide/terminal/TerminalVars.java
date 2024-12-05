package com.pynide.terminal;

import android.annotation.SuppressLint;

import com.pynide.BuildVars;

@SuppressLint("SdCardPath")
public class TerminalVars {
    private static final String FILES = "/data/data/" + BuildVars.PACKAGE_NAME + "/files";
    public static final String HOME = FILES + "/home";
    public static final String PREFIX = FILES + "/usr";
    public static final String TEMP = PREFIX + "/tmp";
    public static final String SHELL = "/system/bin/sh";
}
