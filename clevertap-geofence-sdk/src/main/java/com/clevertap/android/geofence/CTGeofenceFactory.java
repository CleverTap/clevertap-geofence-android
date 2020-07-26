package com.clevertap.android.geofence;

import android.content.Context;

import androidx.annotation.NonNull;

import com.clevertap.android.geofence.interfaces.CTGeofenceAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

class CTGeofenceFactory {

    static CTGeofenceAdapter createGeofenceAdapter(@NonNull Context context) {

        int googlePlayServicesAvailable = GoogleApiAvailability.getInstance().
                isGooglePlayServicesAvailable(context);

        if (Utils.isFusedLocationApiDependencyAvailable()) {

            if (googlePlayServicesAvailable == ConnectionResult.SUCCESS) {
                return new GoogleGeofenceAdapter(context.getApplicationContext());
            } else {
                String errorString = GoogleApiAvailability.getInstance().getErrorString(googlePlayServicesAvailable);
                throw new IllegalStateException("Play service APK error :: " + errorString);
            }
        } else {
            throw new IllegalStateException("play-services-location dependency is missing");
        }
    }
}
