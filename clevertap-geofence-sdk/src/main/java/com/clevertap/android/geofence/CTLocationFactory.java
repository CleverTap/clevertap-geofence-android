package com.clevertap.android.geofence;

import android.content.Context;

import com.clevertap.android.geofence.interfaces.CTLocationAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

class CTLocationFactory {

    static CTLocationAdapter createLocationAdapter(Context context) {

        int googlePlayServicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);

        if (Utils.isFusedLocationApiDependencyAvailable()) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "FusedLocationApi dependency is available");

            if (googlePlayServicesAvailable == ConnectionResult.SUCCESS) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Play service APK is available");
                return new GoogleLocationAdapter(context.getApplicationContext());
            } else {

                String errorString = GoogleApiAvailability.getInstance().getErrorString(googlePlayServicesAvailable);

                if (errorString!=null) {
                    CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                            "Play service APK error :: " +errorString);
                }
                return new AndroidLocationAdapter(context.getApplicationContext());
            }

        } else {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "FusedLocationApi dependency is not available");
            return new AndroidLocationAdapter(context.getApplicationContext());
        }
    }
}
