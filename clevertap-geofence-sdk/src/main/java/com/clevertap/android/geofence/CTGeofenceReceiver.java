package com.clevertap.android.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CTGeofenceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Geofence receiver called");
            CTGeofenceService.enqueueWork(context.getApplicationContext(), intent);
        }
        catch (Exception e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Exception while processing geofence receiver intent");
            e.printStackTrace();
        }
    }
}
