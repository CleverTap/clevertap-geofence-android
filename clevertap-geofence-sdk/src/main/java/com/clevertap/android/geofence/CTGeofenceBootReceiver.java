package com.clevertap.android.geofence;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.clevertap.android.geofence.CTGeofenceAPI.GEOFENCE_LOG_TAG;

public class CTGeofenceBootReceiver extends BroadcastReceiver {

    private static final long BROADCAST_INTENT_TIME_MS = 2000;

    @Override
    public void onReceive(final Context context, Intent intent) {

        final Context applicationContext = context.getApplicationContext();

        CTGeofenceAPI.getLogger().debug(GEOFENCE_LOG_TAG, "onReceive called after " +
                "device reboot");

        if (intent == null) {
            return;
        }

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            if (!Utils.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "We don't have ACCESS_FINE_LOCATION permission! Not registering " +
                                "geofences and location updates after device reboot");
                return;
            }

            if (!Utils.hasBackgroundLocationPermission(context)) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "We don't have ACCESS_BACKGROUND_LOCATION permission! not registering " +
                                "geofences and location updates after device reboot");
                return;
            }

            final PendingResult result = goAsync();

            Thread thread = new Thread() {
                public void run() {
                    try {

                        if (!Utils.initCTGeofenceApiIfRequired(applicationContext)) {
                            // if init fails then return without doing any work
                            finishPendingIntent(result);
                            return;
                        }

                        CTGeofenceAPI.getLogger().debug(GEOFENCE_LOG_TAG,
                                "registering geofences after device reboot");

                        // pass null GeofenceList to register old fences stored in file
                        GeofenceUpdateTask geofenceUpdateTask =
                                new GeofenceUpdateTask(applicationContext, null);

                        Future<?> futureGeofence = CTGeofenceTaskManager.getInstance()
                                .postAsyncSafely("ProcessGeofenceUpdatesOnBoot", geofenceUpdateTask);

                        try {
                            futureGeofence.get(BROADCAST_INTENT_TIME_MS, TimeUnit.MILLISECONDS);
                        } catch (TimeoutException e) {
                            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                    "Timeout geofence update task execution limit of 2 secs");
                        } catch (Exception e) {
                            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                    "Exception while executing geofence update task");
                            e.printStackTrace();
                        }

                        CTGeofenceAPI.getLogger().debug(GEOFENCE_LOG_TAG,
                                "registering location updates after device reboot");

                        LocationUpdateTask locationUpdateTask = new LocationUpdateTask(applicationContext);

                        Future<?> futureLocation = CTGeofenceTaskManager.getInstance()
                                .postAsyncSafely("IntitializeLocationUpdatesOnBoot", locationUpdateTask);

                        try {
                            futureLocation.get(BROADCAST_INTENT_TIME_MS, TimeUnit.MILLISECONDS);
                        } catch (TimeoutException e) {
                            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                    "Timeout location update task execution limit of 2 secs");
                        } catch (Exception e) {
                            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                    "Exception while executing location update task");
                            e.printStackTrace();
                        }

                        finishPendingIntent(result);


                    } catch (Exception e) {

                        finishPendingIntent(result);

                        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                "Exception while processing Boot receiver intent");
                        e.printStackTrace();
                    }
                }
            };
            thread.start();

        }

    }

    private void finishPendingIntent(PendingResult result) {
        if (result != null) {
            result.finish();

            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Boot receiver Pending Intent is finished");
        }
    }
}
