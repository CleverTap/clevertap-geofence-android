package com.clevertap.android.geofence;

import android.content.Context;

import com.clevertap.android.geofence.interfaces.CTLocationAdapter;

 class CTLocationFactory {

    static CTLocationAdapter createLocationAdapter(Context context){

        if (Utils.isFusedLocationApiDependencyAvailable()){
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "FusedLocationApi dependency is available");
            return new GoogleLocationAdapter(context.getApplicationContext());
        }else {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "FusedLocationApi dependency is not available");
            return new AndroidLocationAdapter(context.getApplicationContext());
        }
    }
}
