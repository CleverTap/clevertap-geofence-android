package com.clevertap.android.geofence;

import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.clevertap.android.geofence.interfaces.CTGeofenceTask;
import com.clevertap.android.geofence.interfaces.CTLocationAdapter;

import static android.app.PendingIntent.FLAG_NO_CREATE;

/**
 * Avoids duplicate request of location updates and
 * Requests/Removes Location updates based on change in config settings.
 */
class LocationUpdateTask implements CTGeofenceTask {

    private final Context context;
    @Nullable
    private CTGeofenceSettings ctGeofenceSettings;
    @Nullable
    private final CTLocationAdapter ctLocationAdapter;
    @Nullable
    private OnCompleteListener onCompleteListener;

    LocationUpdateTask(Context context) {
        this.context = context.getApplicationContext();
        ctGeofenceSettings = CTGeofenceAPI.getInstance(this.context).getGeofenceSettings();
        ctLocationAdapter = CTGeofenceAPI.getInstance(this.context).getCtLocationAdapter();
    }

    @WorkerThread
    @Override
    public void execute() {

        if (ctLocationAdapter == null)
            return;

        if (ctGeofenceSettings == null) {
            ctGeofenceSettings = CTGeofenceAPI.getInstance(context).initDefaultConfig();
        }

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Executing LocationUpdateTask...");

        // FLAG_NO_CREATE will tell us if pending intent already exists and is active
        PendingIntent locationPendingIntent = PendingIntentFactory.getPendingIntent(context,
                PendingIntentFactory.PENDING_INTENT_LOCATION, FLAG_NO_CREATE);


        // if background location disabled and if location update request is already registered then remove it
        if (!this.ctGeofenceSettings.isBackgroundLocationUpdatesEnabled() && locationPendingIntent != null) {
            ctLocationAdapter.removeLocationUpdates(locationPendingIntent);
        } else if (isRequestLocation(locationPendingIntent)) {

            // if background location enabled and if location update request is not already registered
            // or there is change in accuracy or fetch mode settings then request location updates
            ctLocationAdapter.requestLocationUpdates();
        } else {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Dropping duplicate location update request");
        }

        // write new settings to file
        Utils.writeSettingsToFile(context, ctGeofenceSettings);

        if (onCompleteListener != null) {
            onCompleteListener.onComplete();
        }

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Finished executing LocationUpdateTask");
    }

    private boolean isRequestLocation(PendingIntent locationPendingIntent) {

        int lastAccuracy = -1;
        int lastFetchMode = -1;
        long lastInterval = -1;
        long lastFastestInterval = -1;
        float lastDisplacement = -1;

        @SuppressWarnings("ConstantConditions") // ctGeofenceSettings won't be null
                int currentAccuracy = ctGeofenceSettings.getLocationAccuracy();
        int currentFetchMode = ctGeofenceSettings.getLocationFetchMode();
        long currentInterval = ctGeofenceSettings.getInterval();
        long currentFastestInterval = ctGeofenceSettings.getFastestInterval();
        float currentDisplacement = ctGeofenceSettings.getSmallestDisplacement();


        // read settings from file
        CTGeofenceSettings lastGeofenceSettings = Utils.readSettingsFromFile(context);
        if (lastGeofenceSettings != null) {
            lastAccuracy = lastGeofenceSettings.getLocationAccuracy();
            lastFetchMode = lastGeofenceSettings.getLocationFetchMode();
            lastInterval = lastGeofenceSettings.getInterval();
            lastFastestInterval = lastGeofenceSettings.getFastestInterval();
            lastDisplacement = lastGeofenceSettings.getSmallestDisplacement();
        }

        boolean isCurrentLocationFetchModeChanged =
                currentFetchMode == CTGeofenceSettings.FETCH_CURRENT_LOCATION_PERIODIC &&
                        (currentAccuracy != lastAccuracy || currentInterval != lastInterval
                                || currentFastestInterval != lastFastestInterval
                                || currentDisplacement != lastDisplacement);

        boolean isLastLocationFetchModeChanged =
                currentFetchMode == CTGeofenceSettings.FETCH_LAST_LOCATION_PERIODIC &&
                        currentInterval != lastInterval;

        return ctGeofenceSettings.isBackgroundLocationUpdatesEnabled() &&
                (locationPendingIntent == null || isCurrentLocationFetchModeChanged
                        || isLastLocationFetchModeChanged || currentFetchMode != lastFetchMode);
    }

    @Override
    public void setOnCompleteListener(@NonNull OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }


}
