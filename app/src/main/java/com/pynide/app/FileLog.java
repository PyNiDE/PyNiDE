package com.pynide.app;

import android.util.Log;

import com.pynide.ui.LaunchActivity;

import org.telegram.messanger.time.FastDateFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;

public class FileLog {
    private OutputStreamWriter streamWriter = null;
    private FastDateFormat dateFormat = null;
    private FastDateFormat fileDateFormat = null;
    private DispatchQueue logQueue = null;

    private File currentFile = null;
    private boolean initied;
    private final static String tag = "pynide";

    private static volatile FileLog Instance = null;

    public static FileLog getInstance() {
        FileLog localInstance = Instance;
        if (localInstance == null) {
            synchronized (FileLog.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new FileLog();
                }
            }
        }
        return localInstance;
    }

    public FileLog() {
        if (!BuildVars.LOGS_ENABLED) {
            return;
        }
        init();
    }

    public void init() {
        if (initied) {
            return;
        }
        dateFormat = FastDateFormat.getInstance("dd_MM_yyyy_HH_mm_ss.SSS", Locale.US);
        fileDateFormat = FastDateFormat.getInstance("dd_MM_yyyy_HH_mm_ss", Locale.US);
        String date = fileDateFormat.format(System.currentTimeMillis());
        try {
            File dir = AndroidUtilities.getLogsDir();
            if (dir == null) {
                return;
            }
            currentFile = new File(dir, date + ".txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            logQueue = new DispatchQueue("logQueue");
            currentFile.createNewFile();
            FileOutputStream stream = new FileOutputStream(currentFile);
            streamWriter = new OutputStreamWriter(stream);
            streamWriter.write("-----start log " + date + "-----\n");
            streamWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initied = true;
    }

    public static void ensureInitied() {
        getInstance().init();
    }

    public static void e(final String message, final Throwable exception) {
        if (!BuildVars.LOGS_ENABLED) {
            return;
        }
        ensureInitied();
        Log.e(tag, message, exception);
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/pynide: " + message + "\n");
                    getInstance().streamWriter.write(exception.toString());
                    StackTraceElement[] stack = exception.getStackTrace();
                    for (StackTraceElement stackTraceElement : stack) {
                        getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/pynide: \tat " + stackTraceElement + "\n");
                    }
                    getInstance().streamWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void e(final String message) {
        if (!BuildVars.LOGS_ENABLED) {
            return;
        }
        ensureInitied();
        Log.e(tag, message);
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/pynide: " + message + "\n");
                    getInstance().streamWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void e(final Throwable e) {
        if (!BuildVars.LOGS_ENABLED) {
            return;
        }
        ensureInitied();
        e.printStackTrace();
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/pynide: " + e + "\n");
                    StackTraceElement[] stack = e.getStackTrace();
                    for (StackTraceElement stackTraceElement : stack) {
                        getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/pynide: \tat " + stackTraceElement + "\n");
                    }
                    Throwable cause = e.getCause();
                    if (cause != null) {
                        getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/pynide: Caused by " + cause + "\n");
                        stack = cause.getStackTrace();
                        for (StackTraceElement stackTraceElement : stack) {
                            getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/pynide: \tat " + stackTraceElement + "\n");
                        }
                    }
                    getInstance().streamWriter.flush();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
        } else {
            e.printStackTrace();
        }
    }

    public static void fatal(final Throwable e) {
        if (!BuildVars.LOGS_ENABLED) {
            return;
        }
        ensureInitied();
        e.printStackTrace();
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " FATAL/pynide: " + e + "\n");
                    StackTraceElement[] stack = e.getStackTrace();
                    for (StackTraceElement traceElement : stack) {
                        getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " FATAL/pynide: \tat " + traceElement + "\n");
                    }
                    Throwable cause = e.getCause();
                    if (cause != null) {
                        getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/pynide: Caused by " + cause + "\n");
                        stack = cause.getStackTrace();
                        for (StackTraceElement stackTraceElement : stack) {
                            getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " E/pynide: \tat " + stackTraceElement + "\n");
                        }
                    }
                    getInstance().streamWriter.flush();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
        } else {
            e.printStackTrace();
        }
    }

    public static void d(final String message) {
        if (!BuildVars.LOGS_ENABLED) {
            return;
        }
        ensureInitied();
        Log.d(tag, message);
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " D/pynide: " + message + "\n");
                    getInstance().streamWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void w(final String message) {
        if (!BuildVars.LOGS_ENABLED) {
            return;
        }
        ensureInitied();
        Log.w(tag, message);
        if (getInstance().streamWriter != null) {
            getInstance().logQueue.postRunnable(() -> {
                try {
                    getInstance().streamWriter.write(getInstance().dateFormat.format(System.currentTimeMillis()) + " W/pynide: " + message + "\n");
                    getInstance().streamWriter.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void cleanupLogs() {
        ensureInitied();
        File dir = AndroidUtilities.getLogsDir();
        if (dir == null) {
            return;
        }
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (getInstance().currentFile != null && file.getAbsolutePath().equals(getInstance().currentFile.getAbsolutePath())) {
                    continue;
                }
                file.delete();
            }
        }
    }
}
