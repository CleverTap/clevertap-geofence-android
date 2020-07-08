package com.clevertap.android.geofence;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.android.gms.location.LocationResult;

public class CTLocationUpdateService extends JobIntentService {

    /**
     * Unique job ID for this service, must be the same value for all work
     * enqueued for the same class.
     */
    static final int JOB_ID = 1000;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, CTLocationUpdateService.class, JOB_ID, work);
    }


    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        CTGeofenceAPI.getInstance(getApplicationContext()).getGeofenceInterface()
                .setLocationForGeofences(LocationResult.extractResult(intent).getLastLocation());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "destroying location update service..");
    }
}
