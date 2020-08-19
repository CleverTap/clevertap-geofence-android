package com.clevertap.android.geofence;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.clevertap.android.geofence.interfaces.CTGeofenceTask;
import com.clevertap.android.geofence.interfaces.CTLocationUpdatesListener;
import com.google.android.gms.location.LocationResult;

import java.util.concurrent.Future;

class PushLocationEventTask implements CTGeofenceTask {

    private final Context context;
    @NonNull
    private final LocationResult locationResult;
    @Nullable
    private OnCompleteListener onCompleteListener;

    PushLocationEventTask(Context context, @NonNull LocationResult locationResult) {
        this.context = context.getApplicationContext();
        this.locationResult = locationResult;
    }

    @WorkerThread
    @Override
    public void execute() {

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Executing PushLocationEventTask...");

        if (!Utils.initCTGeofenceApiIfRequired(context)) {
            // if init fails then return without doing any work
            sendOnCompleteEvent();
            return;
        }

        try {
            Utils.notifyLocationUpdates(context,locationResult.getLastLocation());

            @SuppressWarnings("ConstantConditions") //getCleverTapApi() won't be null here
                    Future<?> future = null;

            if (locationResult.getLastLocation()!=null)
            {
                future = CTGeofenceAPI.getInstance(context)
                        .processTriggeredLocation(locationResult.getLastLocation());
            }

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
        } finally {
            sendOnCompleteEvent();
        }


    }

    private void sendOnCompleteEvent() {
        if (onCompleteListener != null) {
            onCompleteListener.onComplete();
        }
    }

    @Override
    public void setOnCompleteListener(@NonNull OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }
}
