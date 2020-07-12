package com.clevertap.android.geofence;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.clevertap.android.geofence.CTGeofenceAPI.GEOFENCE_LOG_TAG;

public class CTGeofenceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //TODO can getAction() be null?

        if (intent == null || intent.getAction() == null) {
            return;
        }

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            CTGeofenceAPI.getLogger().debug(GEOFENCE_LOG_TAG, "onReceive called after " +
                    "device reboot");

            if (!Utils.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "We don't have ACCESS_FINE_LOCATION permission! Not registering " +
                                "geofences and location updates after device reboot");
                return;
            }

            if (!Utils.hasPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "We don't have ACCESS_BACKGROUND_LOCATION permission! not registering " +
                                "geofences and location updates after device reboot");
                return;
            }

            Intent jobIntent = new Intent();
            jobIntent.putExtra(CTGeofenceConstants.EXTRA_JOB_SERVICE_TYPE,
                    CTGeofenceConstants.JOB_TYPE_DEVICE_BOOT);
            CTLocationUpdateService.enqueueWork(context.getApplicationContext(), jobIntent);
        }

    }
}
