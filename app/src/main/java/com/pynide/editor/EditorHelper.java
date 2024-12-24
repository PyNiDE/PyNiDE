package com.pynide.editor;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.Utils;

import com.pynide.model.AssetFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EditorHelper {
    public static final String ASSETS_FONT_PREFIX = "fonts/";

    private static List<AssetFile> assetFonts = null;

    @NonNull
    public static List<AssetFile> getAssetFonts() {
        if (assetFonts == null) {
            try {
                assetFonts = Arrays.stream(Utils.getApp().getAssets().list(ASSETS_FONT_PREFIX))
                        .filter(s -> s.endsWith(".ttf"))
                        .map(AssetFile::new)
                        .sorted(Comparator.comparing(s -> s.displayName))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                throw new RuntimeException("Failed to load fonts from assets because " + e.getMessage());
            }
        }
        return assetFonts;
    }
}
