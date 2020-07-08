package com.clevertap.android.geofence.model;

import com.clevertap.android.geofence.Logger.LogLevel;

import java.util.Objects;

public class CTGeofenceSettings {


    private final boolean backgroundLocationUpdates;
    private final int locationAccuracy;
    private final int locationFetchMode; // WorkManager or BroadcastReceiver
    private final LogLevel logLevel;

    public static final int ACCURACY_HIGH=1;
    public static final int ACCURACY_MEDIUM=2;
    public static final int ACCURACY_LOW=3;

    public static final int FETCH_AUTO=1;
    public static final int FETCH_MANUAL=2;


    private CTGeofenceSettings(Builder builder) {
        backgroundLocationUpdates = builder.backgroundLocationUpdates;
        locationAccuracy = builder.locationAccuracy;
        locationFetchMode = builder.locationFetchMode;
        logLevel=builder.logLevel;
    }

    public static final class Builder {

        private boolean backgroundLocationUpdates = true;
        private int locationAccuracy = ACCURACY_HIGH;
        private int locationFetchMode = FETCH_MANUAL;
        private LogLevel logLevel = LogLevel.DEBUG;

        public Builder() {

        }

        public CTGeofenceSettings.Builder enableBackgroundLocationUpdates(boolean backgroundLocationUpdates) {
            this.backgroundLocationUpdates = backgroundLocationUpdates;
            return this;
        }

        public CTGeofenceSettings.Builder setLocationAccuracy(int locationAccuracy) {
            this.locationAccuracy = locationAccuracy;
            return this;
        }

        public CTGeofenceSettings.Builder setLocationFetchMode(int locationFetchMode) {
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
