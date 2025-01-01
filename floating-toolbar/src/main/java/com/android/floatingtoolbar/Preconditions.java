package com.android.floatingtoolbar;

class Preconditions {
    public static void checkState(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalStateException(errorMessage);
        }
    }

    public static void checkState(boolean expression) {
        checkState(expression, null);
    }
}
