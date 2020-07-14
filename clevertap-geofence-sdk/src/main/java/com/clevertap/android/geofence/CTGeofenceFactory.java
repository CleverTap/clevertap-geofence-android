package com.clevertap.android.geofence;

import android.content.Context;

import com.clevertap.android.geofence.interfaces.CTGeofenceAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

class CTGeofenceFactory {

    static CTGeofenceAdapter createGeofenceAdapter(Context context) {

        int googlePlayServicesAvailable = GoogleApiAvailability.getInstance().
                isGooglePlayServicesAvailable(context);

        if (Utils.isFusedLocationApiDependencyAvailable() &&
                googlePlayServicesAvailable == ConnectionResult.SUCCESS) {
            return new GoogleGeofenceAdapter(context.getApplicationContext());
        } else {
            return new AndroidGeofenceAdapter(context.getApplicationContext());
        }
    }
}
