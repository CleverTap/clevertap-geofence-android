package com.clevertap.android.geofence;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.WorkerThread;
import androidx.core.content.ContextCompat;

import com.clevertap.android.sdk.CleverTapAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class Utils {

    private static Boolean isPlayServicesDependencyAvailable;
    private static Boolean isFusedLocationDependencyAvailable;
    private static Boolean isConcurrentFuturesDependencyAvailable;

    static boolean hasPermission(final Context context, String permission) {
        try {
            return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, permission);
        } catch (Throwable t) {
            return false;
        }
    }

    static boolean hasBackgroundLocationPermission(final Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return hasPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            } else {
                return true;
            }
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Checks if Google Play services dependency is available.
     *
     * @return <code>true</code> if available, otherwise <code>false</code>.
     */
    static boolean isPlayServicesDependencyAvailable() {

        if (isPlayServicesDependencyAvailable == null) {//use reflection only once
            // Play Services
            try {
                Class.forName("com.google.android.gms.common.GooglePlayServicesUtil");
                isPlayServicesDependencyAvailable = true;
            } catch (ClassNotFoundException e) {
                isPlayServicesDependencyAvailable = false;
            }
        }

        return isPlayServicesDependencyAvailable;
    }

    /**
     * Checks if Google Play services dependency is available for Fused Location.
     *
     * @return <code>true</code> if available, otherwise <code>false</code>.
     */
    static boolean isFusedLocationApiDependencyAvailable() {

        if (isFusedLocationDependencyAvailable == null) {//use reflection only once
            if (!isPlayServicesDependencyAvailable()) {
                isFusedLocationDependencyAvailable = false;
            } else {
                try {
                    Class.forName("com.google.android.gms.location.FusedLocationProviderClient");
                    isFusedLocationDependencyAvailable = true;
                } catch (ClassNotFoundException e) {
                    isFusedLocationDependencyAvailable = false;
                }
            }
        }

        return isFusedLocationDependencyAvailable;
    }

    /**
     * Checks if Google Play services dependency is available.
     *
     * @return <code>true</code> if available, otherwise <code>false</code>.
     */
    static boolean isConcurrentFuturesDependencyAvailable() {

        if (isConcurrentFuturesDependencyAvailable == null) {//use reflection only once
            // concurrent futures
            try {
                Class.forName("androidx.concurrent.futures.CallbackToFutureAdapter");
                isConcurrentFuturesDependencyAvailable = true;
            } catch (ClassNotFoundException e) {
                isConcurrentFuturesDependencyAvailable = false;
            }
        }

        return isConcurrentFuturesDependencyAvailable;
    }

    static String emptyIfNull(String str) {
        return str == null ? "" : str;
    }

    static List<String> jsonToGeoFenceList(JSONObject jsonObject) {
        ArrayList<String> geofenceIdList = new ArrayList<>();
        try {
            JSONArray array = jsonObject.getJSONArray("geofences");

            for (int i = 0; i < array.length(); i++) {

                JSONObject object = array.getJSONObject(i);
                geofenceIdList.add(object.getString(CTGeofenceConstants.KEY_ID));
            }
        } catch (JSONException e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Could not convert JSON to GeofenceIdList - " + e.getMessage());
            e.printStackTrace();
        }
        return geofenceIdList;
    }


    static CTGeofenceSettings readSettingsFromFile(Context context) {

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Reading settings from file...");

        CTGeofenceSettings ctGeofenceSettings = null;

        String settingsString = FileUtils.readFromFile(context,
                FileUtils.getCachedFullPath(context, CTGeofenceConstants.SETTINGS_FILE_NAME));
        if (settingsString != null && !settingsString.trim().equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(settingsString);

                ctGeofenceSettings = new CTGeofenceSettings.Builder()
                        .enableBackgroundLocationUpdates(jsonObject.getBoolean(CTGeofenceConstants.KEY_LAST_BG_LOCATION_UPDATES))
                        .setLocationAccuracy((byte) jsonObject.getInt(CTGeofenceConstants.KEY_LAST_ACCURACY))
                        .setLocationFetchMode((byte) jsonObject.getInt(CTGeofenceConstants.KEY_LAST_FETCH_MODE))
                        .setDebugLevel(Logger.LogLevel.valueOf(jsonObject.getInt(CTGeofenceConstants.KEY_LAST_LOG_LEVEL)))
                        .setGeofenceMonitoringCount(jsonObject.getInt(CTGeofenceConstants.KEY_LAST_GEO_COUNT))
                        .setId(jsonObject.getString(CTGeofenceConstants.KEY_ID))
                        .build();

                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Read settings successfully from file");

            } catch (Exception e) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Failed to read geofence settings from file");
            }
        } else {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Settings not found in file...");
        }

        return ctGeofenceSettings;

    }

    static void writeSettingsToFile(Context context, CTGeofenceSettings ctGeofenceSettings) {

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Writing new settings to file...");

        JSONObject settings = new JSONObject();
        try {
            settings.put(CTGeofenceConstants.KEY_LAST_ACCURACY, ctGeofenceSettings.getLocationAccuracy());
            settings.put(CTGeofenceConstants.KEY_LAST_FETCH_MODE, ctGeofenceSettings.getLocationFetchMode());
            settings.put(CTGeofenceConstants.KEY_LAST_BG_LOCATION_UPDATES,
                    ctGeofenceSettings.isBackgroundLocationUpdatesEnabled());
            settings.put(CTGeofenceConstants.KEY_LAST_LOG_LEVEL, ctGeofenceSettings.getLogLevel().intValue());
            settings.put(CTGeofenceConstants.KEY_LAST_GEO_COUNT, ctGeofenceSettings.getGeofenceMonitoringCount());
            settings.put(CTGeofenceConstants.KEY_ID, CTGeofenceAPI.getInstance(context).getAccountId());

            boolean writeJsonToFile = FileUtils.writeJsonToFile(context, FileUtils.getCachedDirName(context),
                    CTGeofenceConstants.SETTINGS_FILE_NAME, settings);

            if (writeJsonToFile) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "New settings successfully written to file");
            } else {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Failed to write new settings to file");
            }

        } catch (JSONException e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Failed to write new settings to file while parsing json");
        }

    }

    @WorkerThread
    static boolean initCTGeofenceApiIfRequired(Context context) {

        CTGeofenceAPI ctGeofenceAPI = CTGeofenceAPI.getInstance(context);

        if (ctGeofenceAPI.getGeofenceInterface() == null) {
            CTGeofenceSettings ctGeofenceSettings = Utils.readSettingsFromFile(context);
            if (ctGeofenceSettings == null) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Could not initialize CT instance! Dropping this call");
                return false;
            }

            ctGeofenceAPI.setGeofenceSettings(ctGeofenceSettings);
            CleverTapAPI.initGeofenceAPI(context, ctGeofenceSettings.getId(), ctGeofenceAPI);

            if (ctGeofenceAPI.getGeofenceInterface() == null) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Critical issue :: After calling  CleverTapAPI.initGeofenceAPI also init is failed! Dropping this call");
                return false;
            }
        }

        return true;
    }

    static JSONArray subArray(JSONArray arr, int fromIndex, int toIndex) {

        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");

        JSONArray jsonArray = new JSONArray();

        try {
            for (int i = fromIndex; i < toIndex; i++) {
                jsonArray.put(arr.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonArray;

    }
}
