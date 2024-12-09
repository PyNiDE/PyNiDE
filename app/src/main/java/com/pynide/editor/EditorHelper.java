package com.pynide.editor;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.Utils;

import com.pynide.utils.Utilities;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import kotlin.collections.MapsKt;

public class EditorHelper {
    @NonNull
    public static Map<String, String> getFontNames() throws IOException {
        final var names = new HashMap<String, String>();
        final var namesArray = Arrays.stream(Utils.getApp().getAssets().list("fonts")).filter(s -> s.endsWith(".ttf"));
        namesArray.forEach(name -> {
            var temp = name.replace('-', ' ');
            final var dotIndex = temp.lastIndexOf('.');
            if (dotIndex != -1) temp = temp.substring(0, dotIndex);
            final var displayName = Utilities.capitalize(temp);
            names.put(name, displayName);
        });
        return MapsKt.toSortedMap(names);
    }
}
