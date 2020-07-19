package com.clevertap.android.geofence;

import android.content.Context;

import com.clevertap.android.geofence.interfaces.CTGeofenceTask;
import com.google.android.gms.location.LocationResult;

import java.util.concurrent.Future;

public class PushLocationEventTask implements CTGeofenceTask {

    private final Context context;
    private final LocationResult locationResult;
    private OnCompleteListener onCompleteListener;

    PushLocationEventTask(Context context, LocationResult locationResult) {
        this.context = context.getApplicationContext();
        this.locationResult = locationResult;
    }

    @Override
    public void execute() {

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Executing PushLocationEventTask...");

        if (!Utils.initCTGeofenceApiIfRequired(context)) {
            // if init fails then return without doing any work
            return;
        }

        try {

            Future<?> future = CTGeofenceAPI.getInstance(context).getGeofenceInterface()
                    .setLocationForGeofences(locationResult.getLastLocation());

            if (future == null) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Dropping location ping event to CT server");
                return;
            }

            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Calling future for setLocationForGeofences()");

            future.get();

            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Finished calling future for setLocationForGeofences()");
        } catch (Exception e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Failed to push location event to CT");
            e.printStackTrace();
        }


    }

    @Override
    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {

    }
}
