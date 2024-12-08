package com.pynide.terminal;

import android.annotation.SuppressLint;

import com.pynide.BuildVars;

public class TerminalVars {
    @SuppressLint("SdCardPath")
    public static final String FILES_PATH = "/data/data/" + BuildVars.PACKAGE_NAME + "/files";
    public static final String HOME_PATH = FILES_PATH + "/home";
    public static final String PREFIX_PATH = FILES_PATH + "/usr";
}
