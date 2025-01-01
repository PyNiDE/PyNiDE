package com.pynide.model;

import androidx.annotation.NonNull;

import com.pynide.utils.Utilities;

public class AssetFile {
    public final String assetName;
    public final String displayName;

    public AssetFile(@NonNull final String assetName, @NonNull final String displayName) {
        this.assetName = assetName;
        this.displayName = displayName;
    }

    public AssetFile(@NonNull final String assetName) {
        this.assetName = assetName;

        var name = assetName.replace('-', ' ');
        final var dotIndex = name.lastIndexOf('.');
        if (dotIndex != -1) name = name.substring(0, dotIndex);
        this.displayName = Utilities.capitalize(name);
    }

    @NonNull
    @Override
    public String toString() {
        return displayName;
    }
}
