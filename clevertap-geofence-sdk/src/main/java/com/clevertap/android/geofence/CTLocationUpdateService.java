package com.clevertap.android.geofence;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.android.gms.location.LocationResult;

import static com.clevertap.android.geofence.CTGeofenceAPI.GEOFENCE_LOG_TAG;

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

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Handling work in CTLocationUpdateService...");

        if (!Utils.initCTGeofenceApiIfRequired(getApplicationContext()))
        {
            // if init fails then return without doing any work
            return;
        }

        int jobType = intent.getIntExtra(CTGeofenceConstants.EXTRA_JOB_SERVICE_TYPE, -1);

        if (jobType == CTGeofenceConstants.JOB_TYPE_DEVICE_BOOT) {

            CTGeofenceAPI.getLogger().debug(GEOFENCE_LOG_TAG,"registering geofences after device reboot");

            // pass null GeofenceList to register old fences stored in file
            GeofenceUpdateTask geofenceUpdateTask = new GeofenceUpdateTask(getApplicationContext(), null);

            CTGeofenceTaskManager.getInstance().postAsyncSafely("ProcessGeofenceUpdatesOnBoot",
                    geofenceUpdateTask);

            CTGeofenceAPI.getLogger().debug(GEOFENCE_LOG_TAG, "registering location updates after device reboot");

            LocationUpdateTask locationUpdateTask = new LocationUpdateTask(getApplicationContext());

            CTGeofenceTaskManager.getInstance().postAsyncSafely("IntitializeLocationUpdatesOnBoot",
                    locationUpdateTask);

        } else {

            Location location = LocationResult.extractResult(intent).getLastLocation();
            if (location!=null) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "New Location = "+location.getLatitude()+","+ location.getLongitude());
            }

            CTGeofenceAPI.getInstance(getApplicationContext()).getGeofenceInterface()
                    .setLocationForGeofences(LocationResult.extractResult(intent).getLastLocation());
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "destroying location update service..");
    }
}
