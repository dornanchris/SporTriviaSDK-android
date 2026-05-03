package com.sportrivia.sdk.public_api;

import android.util.Log;

/**
 * Logging system for the SporTrivia SDK.
 */
public final class SporTriviaLogger {

    public static final int DEBUG = 0;
    public static final int INFO = 1;
    public static final int WARNING = 2;
    public static final int ERROR = 3;
    public static final int NONE = 4;

    private static final String TAG = "SporTriviaSDK";
    private static int logLevel = WARNING;
    private static LogHandler logHandler;

    public interface LogHandler {
        void log(int level, String message);
    }

    public static void setLogLevel(int level) { logLevel = level; }
    public static int getLogLevel() { return logLevel; }
    public static void setLogHandler(LogHandler handler) { logHandler = handler; }

    public static void debug(String message) { log(DEBUG, message); }
    public static void info(String message) { log(INFO, message); }
    public static void warning(String message) { log(WARNING, message); }
    public static void error(String message) { log(ERROR, message); }

    private static void log(int level, String message) {
        if (level < logLevel) return;
        if (logHandler != null) {
            logHandler.log(level, message);
            return;
        }
        switch (level) {
            case DEBUG:   Log.d(TAG, message); break;
            case INFO:    Log.i(TAG, message); break;
            case WARNING: Log.w(TAG, message); break;
            case ERROR:   Log.e(TAG, message); break;
        }
    }

    static String levelName(int level) {
        switch (level) {
            case DEBUG: return "DEBUG";
            case INFO: return "INFO";
            case WARNING: return "WARN";
            case ERROR: return "ERROR";
            default: return "UNKNOWN";
        }
    }
}
