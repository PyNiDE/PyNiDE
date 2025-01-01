package com.pynide.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.util.Pair;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.CleanUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.Utils;

import com.pynide.BuildVars;
import com.pynide.R;
import com.pynide.terminal.TerminalVars;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AndroidUtilities {
    private static final byte[] ELF_MAGIC = {0x7F, 'E', 'L', 'F'};
    private static final char SHEBANG_START = '#';
    private static final char SHEBANG_DELIMITER = '!';

    private static Map<String, String> environmentVars = null;

    private static final Hashtable<String, Typeface> typefaceCache = new Hashtable<>();

    @Nullable
    public static Typeface getTypeface(@NonNull final String assetPath) {
        synchronized (typefaceCache) {
            if (!typefaceCache.containsKey(assetPath)) {
                try {
                    Typeface typeface;
                    if (Build.VERSION.SDK_INT >= 26) {
                        Typeface.Builder builder = new Typeface.Builder(Utils.getApp().getAssets(), assetPath);
                        if (assetPath.contains("medium")) {
                            builder.setWeight(700);
                        }
                        if (assetPath.contains("italic")) {
                            builder.setItalic(true);
                        }
                        typeface = builder.build();
                    } else {
                        typeface = Typeface.createFromAsset(Utils.getApp().getAssets(), assetPath);
                    }
                    typefaceCache.put(assetPath, typeface);
                } catch (Exception e) {
                    FileLog.e("Could not get typeface '" + assetPath + "' because " + e.getMessage());
                    return null;
                }
            }
            return typefaceCache.get(assetPath);
        }
    }

    public static void toggleIme(@Nullable final Window window, @Nullable final View view, final boolean show) {
        if (window == null || view == null) return;
        final var insetsController = WindowCompat.getInsetsController(window, view);
        if (show) {
            view.requestFocus();
            insetsController.show(WindowInsetsCompat.Type.ime());
        } else {
            insetsController.hide(WindowInsetsCompat.Type.ime());
            view.clearFocus();
        }
    }

    public static void showIme(@Nullable final Window window, @Nullable final View view) {
        toggleIme(window, view, true);
    }

    public static void hideIme(@Nullable final Window window, @Nullable final View view) {
        toggleIme(window, view, false);
    }

    public static void toggleActionBar(@Nullable final ActionBar actionBar, final boolean show) {
        if (actionBar == null) return;
        if (show && !actionBar.isShowing()) {
            actionBar.show();
        } else if (!show && actionBar.isShowing()) {
            actionBar.hide();
        }
    }

    @SuppressLint("RestrictedApi")
    private static void setOptionalIcons(@NonNull final MenuBuilder menuBuilder, final boolean applyInsets) {
        if (!applyInsets) {
            menuBuilder.setOptionalIconsVisible(true);
            return;
        }
        for (final var item : menuBuilder.getVisibleItems()) {
            final var icon = item.getIcon();
            if (icon instanceof InsetDrawable) return;
            if (icon != null && item.requiresOverflow()) {
                final var iconMarginPx = SizeUtils.dp2px(12f);
                item.setIcon(new InsetDrawable(icon, iconMarginPx, 0, iconMarginPx, 0));
            }
        }
    }

    @SuppressLint("RestrictedApi")
    public static void setOptionalIcons(@Nullable final Menu menu, final boolean applyInsets) {
        if (menu == null) return;
        if (menu instanceof MenuBuilder) {
            setOptionalIcons((MenuBuilder) menu, applyInsets);
        }
    }

    public static boolean isNightMode(@Nullable final Configuration configuration) {
        if (configuration == null) return false;
        final var nightModeFlags = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isNightMode(@Nullable final Context context) {
        if (context == null) return false;
        final var configuration = context.getResources().getConfiguration();
        return isNightMode(configuration);
    }

    @Nullable
    public static File getLogsDir() {
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                final var path = Utils.getApp().getExternalFilesDir(null);
                final var dir = new File(path, "logs");
                dir.mkdirs();
                return dir;
            }
        } catch (Exception ignored) {

        }
        try {
            final var dir = new File(Utils.getApp().getCacheDir(), "logs");
            dir.mkdirs();
            return dir;
        } catch (Exception ignored) {

        }
        try {
            final var dir = new File(Utils.getApp().getFilesDir(), "logs");
            dir.mkdirs();
            return dir;
        } catch (Exception ignored) {

        }
        return null;
    }

    @Nullable
    private static String extractShebang(@NonNull final File executable) {
        try (final var inputStream = new FileInputStream(executable)) {
            final var buffer = new byte[256];
            final var bytesRead = inputStream.read(buffer);
            if (bytesRead <= 4) {
                return null;
            }

            if (Arrays.equals(Arrays.copyOfRange(buffer, 0, ELF_MAGIC.length), ELF_MAGIC)) {
                return null;
            }

            if (buffer[0] == SHEBANG_START && buffer[1] == SHEBANG_DELIMITER) {
                var shebang = new String(buffer, 2, bytesRead - 2, StandardCharsets.US_ASCII);
                shebang = shebang.trim();

                if (shebang.startsWith("/usr") || shebang.startsWith("/bin")) {
                    final var binary = shebang.substring(shebang.lastIndexOf('/') + 1);
                    return String.format("%s/bin/%s", TerminalVars.PREFIX_PATH, binary);
                } else if (shebang.startsWith(TerminalVars.FILES_PATH)) {
                    return shebang;
                }
            } else {
                return String.format("%s/bin/sh", TerminalVars.PREFIX_PATH);
            }
        } catch (IOException e) {
            FileLog.e(e);
            return null;
        }
        return null;
    }

    @Nullable
    private static String extractShebang(@NonNull final String executablePath) {
        return extractShebang(new File(executablePath));
    }

    @NonNull
    public static Pair<File, List<String>> wrapCommandArguments(@NonNull final File executable, @NonNull final List<String> arguments, final boolean isLoginShell) {
        final var interpreter = extractShebang(executable);
        final var elf = interpreter == null ? executable.getAbsolutePath() : interpreter;

        final var actualArguments = new ArrayList<String>();
        final var processName = (isLoginShell ? "-" : "") + executable.getName();
        actualArguments.add(processName);

        final String actualElf;
        if (DeviceUtils.getSDKVersionCode() >= 29 && elf.startsWith(TerminalVars.FILES_PATH)) {
            actualElf = "/system/bin/linker" + (android.os.Process.is64Bit() ? "64" : "");
            actualArguments.add(elf);
        } else {
            actualElf = elf;
        }

        if (interpreter != null) {
            actualArguments.add(executable.getAbsolutePath());
        }

        actualArguments.addAll(arguments);
        return Pair.create(new File(actualElf), actualArguments);
    }

    @NonNull
    public static Pair<String, String[]> wrapCommandArguments(@NonNull final String executablePath, @NonNull final String[] argumentsArray, final boolean isLoginShell) {
        final var interpreter = extractShebang(executablePath);
        final var elf = interpreter == null ? executablePath : interpreter;

        final var actualArgumentsArray = new String[0];
        final var processName = (isLoginShell ? "-" : "") + FileUtils.getFileName(executablePath);
        ArrayUtils.add(actualArgumentsArray, processName);

        final String actualElf;
        if (DeviceUtils.getSDKVersionCode() >= 29 && elf.startsWith(TerminalVars.FILES_PATH)) {
            actualElf = "/system/bin/linker" + (android.os.Process.is64Bit() ? "64" : "");
            ArrayUtils.add(actualArgumentsArray, elf);
        } else {
            actualElf = elf;
        }

        if (interpreter != null) {
            ArrayUtils.add(actualArgumentsArray, executablePath);
        }

        ArrayUtils.add(actualArgumentsArray, argumentsArray);
        return Pair.create(actualElf, actualArgumentsArray);
    }

    @NonNull
    public static Map<String, String> getEnvironmentVars() {
        if (environmentVars == null) {
            environmentVars = new LinkedHashMap<>();
            var temp = String.format("%s/tmp", TerminalVars.PREFIX_PATH);

            FileUtils.createOrExistsDir(TerminalVars.FILES_PATH);
            FileUtils.createOrExistsDir(TerminalVars.HOME_PATH);
            FileUtils.createOrExistsDir(TerminalVars.PREFIX_PATH);
            FileUtils.createOrExistsDir(temp);

            environmentVars.put("HOME", TerminalVars.HOME_PATH);
            environmentVars.put("PREFIX", TerminalVars.PREFIX_PATH);
            environmentVars.put("TMPDIR", temp);
            environmentVars.put("TMP", temp);
            environmentVars.put("TERM", "xterm-256color");
            environmentVars.put("COLORTERM", "truecolor");
            environmentVars.put("LANG", "en_US.UTF-8");

            temp = String.format("%s/bin/sh", TerminalVars.PREFIX_PATH);
            if (FileUtils.isFileExists(temp)) {
                environmentVars.put("SHELL", temp);
            } else {
                environmentVars.put("SHELL", "/system/bin/sh");
            }

            environmentVars.put("IDE_NAME", Utils.getApp().getString(R.string.app_name));
            environmentVars.put("IDE_DEBUG", String.valueOf(BuildVars.DEBUG_VERSION));
            environmentVars.put("IDE_VERSION", String.format("%s (%s)", BuildVars.VERSION_NAME, BuildVars.VERSION_CODE));
            environmentVars.put("IDE_PACKAGE", BuildVars.PACKAGE_NAME);

            temp = System.getenv("PATH");
            environmentVars.put("PATH", String.format("%s/bin:%s", TerminalVars.PREFIX_PATH, temp));

            temp = System.getenv("LD_LIBRARY_PATH");
            if (temp == null) {
                environmentVars.put("LD_LIBRARY_PATH", String.format("%s/lib", TerminalVars.PREFIX_PATH));
            } else {
                environmentVars.put("LD_LIBRARY_PATH", String.format("%s/lib:%s", TerminalVars.PREFIX_PATH, temp));
            }

            temp = String.format("%s/lib/terminal-exec.so", TerminalVars.PREFIX_PATH);
            if (FileUtils.isFileExists(temp)) {
                environmentVars.put("LD_PRELOAD", temp);

                if (DeviceUtils.getSDKVersionCode() >= 29) {
                    environmentVars.put("BASEDIR", TerminalVars.FILES_PATH);
                    environmentVars.put("PROC_64BIT", String.valueOf(android.os.Process.is64Bit()));
                }
            }
        }
        return environmentVars;
    }

    @NonNull
    public static String[] getEnvironmentVarsArray() {
        final var environmentVars = getEnvironmentVars();
        final var environmentVarsList = new ArrayList<String>(environmentVars.size());
        environmentVars.forEach((key, value) -> environmentVarsList.add(key + "=" + value));
        Collections.sort(environmentVarsList);
        return environmentVarsList.toArray(new String[0]);
    }

    public static void clearTMPDIR() {
        final var tmpDir = String.format("%s/tmp", TerminalVars.PREFIX_PATH);
        CleanUtils.cleanCustomDir(tmpDir);
        FileUtils.createOrExistsDir(tmpDir);
    }
}
