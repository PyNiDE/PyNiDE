package com.pynide.utils;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class Utilities {
    @NonNull
    public static String capitalize(@NonNull final String value) {
        var lastWhitespace = true;
        final var chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isLetter(chars[i])) {
                if (lastWhitespace) chars[i] = Character.toUpperCase(chars[i]);
                lastWhitespace = false;
            } else {
                lastWhitespace = Character.isWhitespace(chars[i]);
            }
        }
        return new String(chars);
    }

    public static String toHexStringColor(@ColorInt final int color) {
        return String.format("#%s", Integer.toHexString(color).substring(2));
    }
}
