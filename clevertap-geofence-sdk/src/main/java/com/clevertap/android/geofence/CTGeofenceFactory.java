package com.clevertap.android.geofence;

import android.content.Context;

import com.clevertap.android.geofence.interfaces.CTGeofenceAdapter;

 class CTGeofenceFactory {

    static CTGeofenceAdapter createGeofenceAdapter(Context context) {

        if (Utils.isFusedLocationApiDependencyAvailable()) {
            return new GoogleGeofenceAdapter(context);
        } else {
            return new AndroidGeofenceAdapter(context);
        }
    }
}
