package com.pynide.editor;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.Utils;

import com.pynide.utils.Utilities;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class EditorHelper {
    public static final String ASSETS_FONT_PREFIX = "fonts/";

    private static final Map<String, String> fontsCache = new LinkedHashMap<>();

    @NonNull
    public static Map<String, String> getFonts() throws IOException {
        synchronized (fontsCache) {
            if (fontsCache.isEmpty()) {
                final var tempArray = Arrays.stream(Utils.getApp().getAssets().list(ASSETS_FONT_PREFIX)).filter(s -> s.endsWith(".ttf"));
                tempArray.forEach(name -> {
                    var temp = name.replace('-', ' ');
                    final var dotIndex = temp.lastIndexOf('.');
                    if (dotIndex != -1) temp = temp.substring(0, dotIndex);
                    final var displayName = Utilities.capitalize(temp);
                    fontsCache.put(displayName, name);
                });
            }
            return fontsCache;
        }
    }
}
