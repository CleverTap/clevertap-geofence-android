package com.clevertap.android.geofence;

import com.clevertap.android.geofence.Logger.LogLevel;

public class CTGeofenceSettings {


    private final boolean backgroundLocationUpdates;
    private final byte locationAccuracy;
    private final byte locationFetchMode; // WorkManager or BroadcastReceiver
    private final LogLevel logLevel;
    private final int geofenceMonitoringCount;
    private final String id;

    public static final byte ACCURACY_HIGH = 1;
    public static final byte ACCURACY_MEDIUM = 2;
    public static final byte ACCURACY_LOW = 3;

    public static final byte FETCH_CURRENT_LOCATION_PERIODIC = 1; // BroadcastReceiver // current
    public static final byte FETCH_LAST_LOCATION_PERIODIC = 2; // Work Manager // call getLastLocation()

    public static final int DEFAULT_GEO_MONITOR_COUNT = 50;


    private CTGeofenceSettings(Builder builder) {
        backgroundLocationUpdates = builder.backgroundLocationUpdates;
        locationAccuracy = builder.locationAccuracy;
        locationFetchMode = builder.locationFetchMode;
        logLevel = builder.logLevel;
        geofenceMonitoringCount = builder.geofenceMonitoringCount;
        id = builder.id;
    }

    //TODO add comments for each method
    public static final class Builder {

        private boolean backgroundLocationUpdates = true;
        private byte locationAccuracy = ACCURACY_HIGH;
        private byte locationFetchMode = FETCH_LAST_LOCATION_PERIODIC;
        private LogLevel logLevel = LogLevel.DEBUG;
        private int geofenceMonitoringCount = DEFAULT_GEO_MONITOR_COUNT;
        private String id;

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

        public CTGeofenceSettings.Builder setGeofenceMonitoringCount(int geofenceMonitoringCount) {
            this.geofenceMonitoringCount = geofenceMonitoringCount;
            return this;
        }

        CTGeofenceSettings.Builder setId(String id) {
            this.id = id;
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

    public int getGeofenceMonitoringCount() {
        return geofenceMonitoringCount;
    }

    String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CTGeofenceSettings that = (CTGeofenceSettings) o;
        return backgroundLocationUpdates == that.backgroundLocationUpdates &&
                locationAccuracy == that.locationAccuracy &&
                locationFetchMode == that.locationFetchMode &&
                logLevel == that.logLevel && geofenceMonitoringCount == that.geofenceMonitoringCount
                && id.equals(that.id);
    }
}
