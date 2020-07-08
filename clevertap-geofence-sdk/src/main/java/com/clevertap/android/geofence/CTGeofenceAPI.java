package com.clevertap.android.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;

import com.clevertap.android.geofence.interfaces.CTGeofenceCallback;
import com.clevertap.android.geofence.interfaces.CTGeofenceInterface;
import com.clevertap.android.geofence.interfaces.CTGeofenceAdapter;
import com.clevertap.android.geofence.interfaces.CTLocatioCallback;
import com.clevertap.android.geofence.interfaces.CTLocationAdapter;
import com.clevertap.android.geofence.model.CTGeofence;
import com.clevertap.android.geofence.model.CTGeofenceSettings;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static android.app.PendingIntent.FLAG_NO_CREATE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class CTGeofenceAPI implements CTGeofenceCallback {

    public static final String GEOFENCE_LOG_TAG = "CTGeofence";
    private static CTGeofenceAPI ctGeofenceAPI;
    private final Context context;
    private static Logger logger;
    private final CTLocationAdapter ctLocationAdapter;
    private final CTGeofenceAdapter ctGeofenceAdapter;
    private CTGeofenceSettings ctGeofenceSettings;
    private CTGeofenceInterface ctGeofenceInterface;
    private boolean isActivated;

    private CTGeofenceAPI(Context context) {
        this.context = context;
        logger = new Logger(Logger.LogLevel.DEBUG);
        ctLocationAdapter = CTLocationFactory.createLocationAdapter(context);
        ctGeofenceAdapter = CTGeofenceFactory.createGeofenceAdapter(context);
    }


    public static CTGeofenceAPI getInstance(Context context) {
        if (ctGeofenceAPI == null) {
            synchronized (CTGeofenceAPI.class) {
                if (ctGeofenceAPI == null) {
                    ctGeofenceAPI = new CTGeofenceAPI(context);
                }
            }
        }
        return ctGeofenceAPI;
    }

    /**
     * Set geofence API configuration settings, should be used by CT SDK only
     *
     * @param ctGeofenceSettings
     */
    public void setGeofenceSettings(CTGeofenceSettings ctGeofenceSettings) {
        this.ctGeofenceSettings = ctGeofenceSettings;
    }

    /**
     * register an implementation of CTGeofenceInterface, should be used by CT SDK only
     *
     * @param ctGeofenceInterface
     */
    public void setGeofenceInterface(CTGeofenceInterface ctGeofenceInterface) {
        this.ctGeofenceInterface = ctGeofenceInterface;
    }

    /**
     * Initializes location update receivers and geofence monitoring by
     * reading config settings if provided or will use default settings
     */
    public void activate() {

        if (isActivated) {
            logger.debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Geofence API already activated! dropping this call");
            return;
        }

        if (ctGeofenceInterface == null) {
            throw new IllegalStateException("setGeofenceContract() must be called before activate()");
        }

        if (ctGeofenceSettings == null) {
            ctGeofenceSettings = initDefaultConfig();
        }

        logger.setDebugLevel(ctGeofenceSettings.getLogLevel());


        ctGeofenceInterface.setGeoFenceCallback(this);
        logger.debug(GEOFENCE_LOG_TAG, "geofence callback registered");

        PendingIntent locationPendingIntent = PendingIntentFactory.getPendingIntent(context,
                PendingIntentFactory.PENDING_INTENT_LOCATION, FLAG_NO_CREATE);

        int lastAccuracy = -1;
        int lastFetchMode = -1;
        int currentAccuracy = ctGeofenceSettings.getLocationAccuracy();
        int currentFetchMode = ctGeofenceSettings.getLocationFetchMode();

        // read settings from file
        String settingsString = FileUtils.readFromFile(context, CTGeofenceConstants.SETTINGS_FULL_PATH);
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
                || (currentAccuracy != lastAccuracy && currentFetchMode== CTGeofenceSettings.FETCH_AUTO)
                || currentFetchMode != lastFetchMode)) {

            // if background location enabled and if location update request is not already registered
            // or there is change in accuracy or fetch mode settings then request location updates
            ctLocationAdapter.requestLocationUpdates();
        }

        // write new settings to file
        JSONObject settings = new JSONObject();
        try {
            settings.put(CTGeofenceConstants.KEY_LAST_ACCURACY, currentAccuracy);
            FileUtils.writeJsonToFile(context, CTGeofenceConstants.CACHED_DIR_NAME,
                    CTGeofenceConstants.SETTINGS_FILE_NAME, settings);
        } catch (JSONException e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Failed to write geofence settings to file");
        }

        isActivated = true;

    }

    /**
     * unregisters geofences, location updates and cleanup all resources
     */
    public void deactivate() {

        /**
         * TODO: deactivate fence and location receivers and clean up resources
         */

        // stop geofence monitoring
        PendingIntent geofencePendingIntent = PendingIntentFactory.getPendingIntent(context,
                PendingIntentFactory.PENDING_INTENT_GEOFENCE, FLAG_NO_CREATE);
        ctGeofenceAdapter.stopGeofenceMonitoring(geofencePendingIntent);

        // stop location updates
        PendingIntent locationPendingIntent = PendingIntentFactory.getPendingIntent(context,
                PendingIntentFactory.PENDING_INTENT_LOCATION, FLAG_NO_CREATE);
        ctLocationAdapter.removeLocationUpdates(locationPendingIntent);

        isActivated = false;

    }

    /**
     * fetches last location from FusedLocationAPI and delivers the result on main thread
     */
    public void triggerLocation() {

        if (!isActivated) {
            throw new IllegalStateException("activate() must be called before triggerLocation()");
        }

        ctLocationAdapter.getLastLocation(new CTLocatioCallback() {
            @Override
            public void onLocationComplete(Location location) {
                //get's called on main thread
                ctGeofenceInterface.setLocationForGeofences(location);
            }
        });

    }

    private CTGeofenceSettings initDefaultConfig() {
        return new CTGeofenceSettings.Builder().build();
    }

    @Override
    public void onSuccess(JSONObject fenceList) {
        //thread safe
        if (fenceList != null) {

            //remove previously added geofences
            String oldFenceListString = FileUtils.readFromFile(context, CTGeofenceConstants.CACHED_FULL_PATH);
            if (oldFenceListString != null && !oldFenceListString.trim().equals("")) {

                List<String> ctOldGeofenceList = null;
                try {
                    ctOldGeofenceList = CTGeofence.toIds(new JSONObject(oldFenceListString));
                } catch (Exception e) {
                    CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                            "Failed to read previously registered geofences from file");
                    e.printStackTrace();
                }

                ctGeofenceAdapter.removeAllGeofence(ctOldGeofenceList);
            }


            //add new geofences
            FileUtils.writeJsonToFile(context, CTGeofenceConstants.CACHED_DIR_NAME,
                    CTGeofenceConstants.CACHED_FILE_NAME, fenceList);
            List<CTGeofence> ctGeofenceList = CTGeofence.from(fenceList);

            ctGeofenceAdapter.addAllGeofence(ctGeofenceList);

        }
    }

    @Override
    public void onFailure(Throwable error) {

    }

    CTGeofenceAdapter getCtGeofenceAdapter() {
        return ctGeofenceAdapter;
    }

    CTLocationAdapter getCtLocationAdapter() {
        return ctLocationAdapter;
    }

    CTGeofenceInterface getGeofenceInterface() {
        return ctGeofenceInterface;
    }

    public CTGeofenceSettings getGeofenceSettings() {
        return ctGeofenceSettings;
    }

    public static Logger getLogger() {
        return logger;
    }
}
