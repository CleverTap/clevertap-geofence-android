package com.clevertap.android.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.clevertap.android.geofence.interfaces.CTLocatioCallback;
import com.clevertap.android.geofence.interfaces.CTLocationAdapter;
import com.clevertap.android.geofence.model.CTGeofenceSettings;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.TimeUnit;

import static android.app.PendingIntent.FLAG_NO_CREATE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.clevertap.android.geofence.CTGeofenceConstants.TAG_WORK_LOCATION_UPDATES;

public class GoogleLocationAdapter implements CTLocationAdapter {

    private static final long INTERVAL_IN_MILLIS = 90 * 60 * 1000; // TODO: Exact values
    private static final long INTERVAL_FASTEST_IN_MILLIS = 90 * 60 * 1000;
    private static final float SMALLEST_DISPLACEMENT_IN_METERS = 2;
    private static final long FLEX_INTERVAL_IN_MILLIS = 15 * 60 * 1000;
    private final Context context;
    private final FusedLocationProviderClient fusedProviderClient;
    private boolean backgroundLocationUpdatesEnabled;
    private int locationFetchMode;
    private int locationAccuracy = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private boolean isPlayServicesAvailable;

    GoogleLocationAdapter(Context context) {
        this.context = context.getApplicationContext();
        fusedProviderClient = LocationServices.getFusedLocationProviderClient(this.context);

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this.context) == ConnectionResult.SUCCESS) {
            isPlayServicesAvailable = true;
        }
    }

    @WorkerThread
    @Override
    public void requestLocationUpdates() {
        applySettings(context);

        if (!backgroundLocationUpdatesEnabled) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "not requesting location updates since background location updates is not enabled");
            return;
        }

        if (locationFetchMode == CTGeofenceSettings.FETCH_AUTO) {

            // should get same pendingIntent on each app launch or else instance will leak
            PendingIntent pendingIntent = PendingIntentFactory.getPendingIntent(context,
                    PendingIntentFactory.PENDING_INTENT_LOCATION, FLAG_UPDATE_CURRENT);

            clearLocationWorkRequest();

            try {
                // will overwrite location request if change in location config is detected
                Task<Void> requestLocationUpdatesTask = fusedProviderClient.requestLocationUpdates(getLocationRequest(), pendingIntent);

                // blocking task
                Tasks.await(requestLocationUpdatesTask);
            } catch (Exception e) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Failed to request location updates");
                e.printStackTrace();
            }
        } else {

            // remove previously registered location request
            PendingIntent pendingIntent = PendingIntentFactory.getPendingIntent(context,
                    PendingIntentFactory.PENDING_INTENT_LOCATION, FLAG_NO_CREATE);

            clearLocationUpdates(pendingIntent);

            // start periodic work for location updates
            scheduleManualLocationUpdates();
        }
    }

    private void scheduleManualLocationUpdates() {
        PeriodicWorkRequest locationRequest = new PeriodicWorkRequest.Builder(BackgroundLocationWork.class,
                INTERVAL_IN_MILLIS, TimeUnit.MILLISECONDS,
                FLEX_INTERVAL_IN_MILLIS, TimeUnit.MILLISECONDS)
                .build();

        // schedule unique work request to avoid duplicates
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(TAG_WORK_LOCATION_UPDATES,
                ExistingPeriodicWorkPolicy.KEEP, locationRequest);
    }

    @WorkerThread
    @Override
    public void removeLocationUpdates(PendingIntent pendingIntent) {
        clearLocationUpdates(pendingIntent);
        clearLocationWorkRequest();
    }

    @Override
    public void setLocationAccuracy(int accuracy) {

    }

    @Override
    public void getLastLocation(final CTLocatioCallback callback) {
        //thread safe

        if (callback == null) {
            throw new IllegalArgumentException("location callback can not be null");
        }


        fusedProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                // get's called on main thread
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Last location fetch completed");
                Location lastLocation = task.getResult();
                callback.onLocationComplete(lastLocation);

            }
        });

    }

    @Override
    public int getLocationAccuracy() {
        return 0;
    }

    LocationRequest getLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(INTERVAL_IN_MILLIS);
        locationRequest.setFastestInterval(INTERVAL_FASTEST_IN_MILLIS);
        locationRequest.setPriority(locationAccuracy);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT_IN_METERS);

        return locationRequest;
    }

    private void applySettings(Context context) {
        locationFetchMode = CTGeofenceAPI.getInstance(context).getGeofenceSettings().getLocationFetchMode();
        backgroundLocationUpdatesEnabled = CTGeofenceAPI.getInstance(context).getGeofenceSettings()
                .isBackgroundLocationUpdatesEnabled();

        int accuracy = CTGeofenceAPI.getInstance(context).getGeofenceSettings().getLocationAccuracy();
        switch (accuracy) {
            case 1:
                locationAccuracy = LocationRequest.PRIORITY_HIGH_ACCURACY;
                break;
            case 2:
                locationAccuracy = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
                break;
            case 3:
                locationAccuracy = LocationRequest.PRIORITY_LOW_POWER;
                break;
        }
    }

    private void clearLocationWorkRequest() {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(TAG_WORK_LOCATION_UPDATES);
        } catch (Exception e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Failed to cancel location work request");
            e.printStackTrace();
        }
    }

    @WorkerThread
    private void clearLocationUpdates(PendingIntent pendingIntent) {
        if (pendingIntent == null) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Can't stop location updates since provided pendingIntent is null");
            return;
        }

        try {
            Task<Void> removeLocationUpdatesTask = fusedProviderClient.removeLocationUpdates(pendingIntent);

            // blocking task
            Tasks.await(removeLocationUpdatesTask);

            pendingIntent.cancel();

            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Location updates removed successfully");
        } catch (Exception e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Failed to remove location updates");
            e.printStackTrace();
        }
    }

}
