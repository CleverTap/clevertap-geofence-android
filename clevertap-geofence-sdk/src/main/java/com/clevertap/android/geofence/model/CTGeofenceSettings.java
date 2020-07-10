package com.clevertap.android.geofence.model;

import com.clevertap.android.geofence.Logger.LogLevel;

public class CTGeofenceSettings {


    private final boolean backgroundLocationUpdates;
    private final byte locationAccuracy;
    private final byte locationFetchMode; // WorkManager or BroadcastReceiver
    private final LogLevel logLevel;

    public static final byte ACCURACY_HIGH = 1;
    public static final byte ACCURACY_MEDIUM = 2;
    public static final byte ACCURACY_LOW = 3;

    public static final byte FETCH_AUTO = 1; // BroadcastReceiver // current
    public static final byte FETCH_PERIODIC = 2; // Work Manager // call getLastLocation()


    private CTGeofenceSettings(Builder builder) {
        backgroundLocationUpdates = builder.backgroundLocationUpdates;
        locationAccuracy = builder.locationAccuracy;
        locationFetchMode = builder.locationFetchMode;
        logLevel = builder.logLevel;
    }

    public static final class Builder {

        private boolean backgroundLocationUpdates = true;
        private byte locationAccuracy = ACCURACY_HIGH;
        private byte locationFetchMode = FETCH_PERIODIC;
        private LogLevel logLevel = LogLevel.DEBUG;

        public Builder() {

        }

        public CTGeofenceSettings.Builder enableBackgroundLocationUpdates(boolean backgroundLocationUpdates) {
            this.backgroundLocationUpdates = backgroundLocationUpdates;
            return this;
        }

        public CTGeofenceSettings.Builder setLocationAccuracy(byte locationAccuracy) {
            this.locationAccuracy = locationAccuracy;
            return this;
        }

        public CTGeofenceSettings.Builder setLocationFetchMode(byte locationFetchMode) {
            this.locationFetchMode = locationFetchMode;
            return this;
        }

        public CTGeofenceSettings.Builder setDebugLevel(LogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public CTGeofenceSettings build() {
            CTGeofenceSettings ctGeofenceSettings = new CTGeofenceSettings(this);
            return ctGeofenceSettings;
        }
    }

    public int getLocationAccuracy() {
        return locationAccuracy;
    }

    public int getLocationFetchMode() {
        return locationFetchMode;
    }

    public boolean isBackgroundLocationUpdatesEnabled() {
        return backgroundLocationUpdates;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CTGeofenceSettings that = (CTGeofenceSettings) o;
        return backgroundLocationUpdates == that.backgroundLocationUpdates &&
                locationAccuracy == that.locationAccuracy &&
                locationFetchMode == that.locationFetchMode &&
                logLevel == that.logLevel;
    }
}
