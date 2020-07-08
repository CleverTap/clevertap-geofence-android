package com.clevertap.android.geofence;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

class Utils {

    private static Boolean isPlayServicesDependencyAvailable;
    private static Boolean isFusedLocationDependencyAvailable;

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
}
