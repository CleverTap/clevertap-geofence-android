package com.clevertap.android.geofence;

import android.app.PendingIntent;
import android.content.Context;

import com.clevertap.android.geofence.interfaces.CTGeofenceTask;
import com.clevertap.android.geofence.interfaces.CTLocationAdapter;
import com.clevertap.android.geofence.model.CTGeofenceSettings;

import org.json.JSONException;
import org.json.JSONObject;

import static android.app.PendingIntent.FLAG_NO_CREATE;

/**
 * Avoids duplicate request of location updates and
 * Requests/Removes Location updates based on change in config settings.
 */
class LocationUpdateTask implements CTGeofenceTask {

    private final Context context;
    private final CTGeofenceSettings ctGeofenceSettings;
    private final CTLocationAdapter ctLocationAdapter;
    private OnCompleteListener onCompleteListener;

    LocationUpdateTask(Context context) {
        this.context = context.getApplicationContext();
        ctGeofenceSettings = CTGeofenceAPI.getInstance(this.context).getGeofenceSettings();
        ctLocationAdapter = CTGeofenceAPI.getInstance(this.context).getCtLocationAdapter();
    }

    @Override
    public void execute() {

        // FLAG_NO_CREATE will tell us if pending intent already exists and is active
        PendingIntent locationPendingIntent = PendingIntentFactory.getPendingIntent(context,
                PendingIntentFactory.PENDING_INTENT_LOCATION, FLAG_NO_CREATE);

        int lastAccuracy = -1;
        int lastFetchMode = -1;
        int currentAccuracy = ctGeofenceSettings.getLocationAccuracy();
        int currentFetchMode = ctGeofenceSettings.getLocationFetchMode();

        // read settings from file
        String settingsString = FileUtils.readFromFile(context,
                FileUtils.getCachedFullPath(context,CTGeofenceConstants.SETTINGS_FILE_NAME));
        if (settingsString != null && !settingsString.trim().equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(settingsString);
                lastAccuracy = jsonObject.getInt(CTGeofenceConstants.KEY_LAST_ACCURACY);
                lastFetchMode = jsonObject.getInt(CTGeofenceConstants.KEY_LAST_FETCH_MODE);

            } catch (Exception e) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Failed to read geofence settings from file");
            }
        }


        // if background location disabled and if location update request is already registered then remove it
        if (!ctGeofenceSettings.isBackgroundLocationUpdatesEnabled() && locationPendingIntent != null) {

            ctLocationAdapter.removeLocationUpdates(locationPendingIntent);

        } else if (ctGeofenceSettings.isBackgroundLocationUpdatesEnabled()
                && (locationPendingIntent == null
                || (currentAccuracy != lastAccuracy && currentFetchMode == CTGeofenceSettings.FETCH_AUTO)
                || currentFetchMode != lastFetchMode)) {

            // if background location enabled and if location update request is not already registered
            // or there is change in accuracy or fetch mode settings then request location updates
            ctLocationAdapter.requestLocationUpdates();
        } else {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Dropping duplicate location update request");
        }

        // write new settings to file
        JSONObject settings = new JSONObject();
        try {
            settings.put(CTGeofenceConstants.KEY_LAST_ACCURACY, currentAccuracy);
            settings.put(CTGeofenceConstants.KEY_LAST_FETCH_MODE, currentFetchMode);
            FileUtils.writeJsonToFile(context, FileUtils.getCachedDirName(context),
                    CTGeofenceConstants.SETTINGS_FILE_NAME, settings);
        } catch (JSONException e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Failed to write geofence settings to file");
        }

        if (onCompleteListener != null) {
            onCompleteListener.onComplete();
        }
    }

    @Override
    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }


}
