package com.clevertap.android.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.clevertap.android.geofence.CTGeofenceAPI.GEOFENCE_LOG_TAG;

public class CTGeofenceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        if (intent != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            CTGeofenceAPI.getLogger().debug(GEOFENCE_LOG_TAG, "onReceive called after device reboot");

            Intent jobIntent = new Intent();
            jobIntent.putExtra(CTGeofenceConstants.EXTRA_JOB_SERVICE_TYPE,CTGeofenceConstants.JOB_TYPE_DEVICE_BOOT);
            CTLocationUpdateService.enqueueWork(context.getApplicationContext(),jobIntent);


        }

    }
}
