package com.clevertap.android.geofence;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

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
}
