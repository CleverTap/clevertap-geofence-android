package com.clevertap.android.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CTGeofenceReceiver extends BroadcastReceiver {

    private static final long BROADCAST_INTENT_TIME_MS = 8000;

    @Override
    public void onReceive(final Context context, final Intent intent) {

        final PendingResult result = goAsync();

        try {

            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Geofence receiver called");

            //CTGeofenceService.enqueueWork(context.getApplicationContext(), intent);

            Thread thread = new Thread() {
                public void run() {
                    PushGeofenceEventTask pushGeofenceEventTask = new PushGeofenceEventTask(context, intent);

                    Future<?> future = CTGeofenceTaskManager.getInstance().postAsyncSafely("PushGeofenceEvent",
                            pushGeofenceEventTask);

                    try {
                        future.get(BROADCAST_INTENT_TIME_MS, TimeUnit.MILLISECONDS);
                    }catch (TimeoutException e) {
                        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                "Timeout geofence receiver execution limit of 10 secs");
                    } catch (Exception e) {
                        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                "Exception while processing geofence receiver intent");
                        e.printStackTrace();
                    }

                    if (result != null) {
                        result.finish();

                        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                "Geofence receiver Pending Intent is finished");
                    }
                }
            };
            thread.start();

        }
        catch (Exception e) {

            if (result != null) {
                result.finish();

                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Geofence receiver Pending Intent is finished");
            }

            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Exception while processing geofence receiver intent");
            e.printStackTrace();
        }

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Returning from Geofence receiver");
    }
}
