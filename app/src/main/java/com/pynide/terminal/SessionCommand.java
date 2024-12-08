package com.pynide.terminal;

public class SessionCommand {
    final String executablePath;
    final String[] argumentsArray;

    public SessionCommand(final String executablePath, final String[] argumentsArray) {
        this.executablePath = executablePath;
        this.argumentsArray = argumentsArray;
    }
}
