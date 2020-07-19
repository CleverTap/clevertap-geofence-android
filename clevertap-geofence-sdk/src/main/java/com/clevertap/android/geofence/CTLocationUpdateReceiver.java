package com.clevertap.android.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.LocationResult;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CTLocationUpdateReceiver extends BroadcastReceiver {

    private static final long BROADCAST_INTENT_TIME_MS = 5000;

    @Override
    public void onReceive(final Context context, final Intent intent) {

        final PendingResult result = goAsync();

        try {

            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Location updates receiver called");

            final LocationResult locationResult = LocationResult.extractResult(intent);

            if (locationResult != null && locationResult.getLastLocation() != null) {
                Thread thread = new Thread() {
                    public void run() {

                        PushLocationEventTask pushLocationEventTask =
                                new PushLocationEventTask(context, locationResult);

                        Future<?> future = CTGeofenceTaskManager.getInstance()
                                .postAsyncSafely("PushLocationEvent", pushLocationEventTask);

                        try {
                            future.get(BROADCAST_INTENT_TIME_MS, TimeUnit.MILLISECONDS);
                        } catch (TimeoutException e) {
                            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                    "Timeout location receiver execution limit of 10 secs");
                        } catch (Exception e) {
                            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                    "Exception while processing location receiver intent");
                            e.printStackTrace();
                        }

                        finishPendingIntent(result);
                    }
                };

                thread.start();
            } else {

                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Location Result is null");

                finishPendingIntent(result);
            }

        } catch (Exception e) {

            finishPendingIntent(result);

            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Exception while processing location updates receiver intent");
            e.printStackTrace();
        }

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Returning from Location Updates Receiver");

    }

    private void finishPendingIntent(PendingResult result) {
        if (result != null) {
            result.finish();

            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Location receiver Pending Intent is finished");
        }
    }

}
