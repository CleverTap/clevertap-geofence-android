package com.clevertap.android.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationResult;

class CTLocationUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Location updates receiver called");

            LocationResult result = LocationResult.extractResult(intent);
            if (result != null && result.getLastLocation() != null) {
                CTLocationUpdateService.enqueueWork(context.getApplicationContext(), intent);
            } else {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Location Result is null");
            }
        } catch (Exception e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Exception while processing location updates receiver intent");
            e.printStackTrace();
        }
    }

}
