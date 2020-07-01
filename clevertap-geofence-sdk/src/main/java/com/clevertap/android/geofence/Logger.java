package com.clevertap.android.geofence;

import android.util.Log;

public final class Logger {

    private LogLevel debugLevel;

    public enum LogLevel {
        OFF(-1),
        INFO(0),
        DEBUG(2);

        private final int value;

        LogLevel(final int newValue) {
            value = newValue;
        }

        public int intValue() {
            return value;
        }
    }

    Logger(LogLevel level) {
        setDebugLevel(level);
    }

    public void setDebugLevel(LogLevel level) {
        this.debugLevel = level;
    }

    /**
     * Logs to Debug if the debug level is greater than 1.
     */

    public void debug(String message) {
        if (debugLevel.intValue() > LogLevel.INFO.intValue()) {
            Log.d(CTGeofenceAPI.GEOFENCE_LOG_TAG, message);
        }
    }

    public void debug(String suffix, String message) {
        if (debugLevel.intValue() > LogLevel.INFO.intValue()) {
            if (message.length() > 4000) {
                Log.d(CTGeofenceAPI.GEOFENCE_LOG_TAG + ":" + suffix, message.substring(0, 4000));
                debug(suffix, message.substring(4000));
            } else {
                Log.d(CTGeofenceAPI.GEOFENCE_LOG_TAG + ":" + suffix, message);
            }
        }
    }

    public void debug(String suffix, String message, Throwable t) {
        if (debugLevel.intValue() > LogLevel.INFO.intValue()) {
            Log.d(CTGeofenceAPI.GEOFENCE_LOG_TAG + ":" + suffix, message, t);
        }
    }

    public void debug(String message, Throwable t) {
        if (debugLevel.intValue() > LogLevel.INFO.intValue()) {
            Log.d(CTGeofenceAPI.GEOFENCE_LOG_TAG, message, t);
        }
    }

    /**
     * Logs to Verbose if the debug level is greater than 2.
     */

    public void verbose(String message) {
        if (debugLevel.intValue() > LogLevel.DEBUG.intValue()) {
            Log.v(CTGeofenceAPI.GEOFENCE_LOG_TAG, message);
        }
    }

    public void verbose(String suffix, String message) {
        if (debugLevel.intValue() > LogLevel.DEBUG.intValue()) {
            if (message.length() > 4000) {
                Log.v(CTGeofenceAPI.GEOFENCE_LOG_TAG + ":" + suffix, message.substring(0, 4000));
                verbose(suffix, message.substring(4000));
            } else {
                Log.v(CTGeofenceAPI.GEOFENCE_LOG_TAG + ":" + suffix, message);
            }
        }
    }

    public void verbose(String suffix, String message, Throwable t) {
        if (debugLevel.intValue() > LogLevel.DEBUG.intValue()) {
            Log.v(CTGeofenceAPI.GEOFENCE_LOG_TAG + ":" + suffix, message, t);
        }
    }

    public void verbose(String message, Throwable t) {
        if (debugLevel.intValue() > LogLevel.DEBUG.intValue()) {
            Log.v(CTGeofenceAPI.GEOFENCE_LOG_TAG, message, t);
        }
    }

    /**
     * Logs to Info if the debug level is greater than or equal to 1.
     */

    public void info(String message) {
        if (debugLevel.intValue() >= LogLevel.INFO.intValue()) {
            Log.i(CTGeofenceAPI.GEOFENCE_LOG_TAG, message);
        }
    }

    public void info(String suffix, String message) {
        if (debugLevel.intValue() >= LogLevel.INFO.intValue()) {
            Log.i(CTGeofenceAPI.GEOFENCE_LOG_TAG + ":" + suffix, message);
        }
    }

    public void info(String suffix, String message, Throwable t) {
        if (debugLevel.intValue() >= LogLevel.INFO.intValue()) {
            Log.i(CTGeofenceAPI.GEOFENCE_LOG_TAG + ":" + suffix, message, t);
        }
    }

    public void info(String message, Throwable t) {
        if (debugLevel.intValue() >= LogLevel.INFO.intValue()) {
            Log.i(CTGeofenceAPI.GEOFENCE_LOG_TAG, message, t);
        }
    }

}
