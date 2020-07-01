package com.clevertap.android.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.clevertap.android.geofence.interfaces.CTLocationRepository;
import com.clevertap.android.geofence.model.CTGeofenceSettings;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GoogleLocationRepository implements CTLocationRepository {

    private static final long INTERVAL_IN_MILLIS = 90 * 60 * 1000;
    private static final long INTERVAL_FASTEST_IN_MILLIS = 90 * 60 * 1000;
    private static final float SMALLEST_DISPLACEMENT_IN_METERS = 2;
    private final Context context;
    private final FusedLocationProviderClient fusedProviderClient;
    private boolean backgroundLocationUpdatesEnabled;
    private int locationFetchMode;
    private int locationAccuracy = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private boolean isPlayServicesAvailable;
    private PendingIntent locationPendingIntent;

    GoogleLocationRepository(Context context) {
        this.context = context;
        fusedProviderClient = LocationServices.getFusedLocationProviderClient(context);

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            isPlayServicesAvailable = true;

            locationFetchMode = CTGeofenceAPI.getInstance(context).getGeofenceSettings().getLocationFetchMode();
            backgroundLocationUpdatesEnabled = CTGeofenceAPI.getInstance(context).getGeofenceSettings().isBackgroundLocationUpdatesEnabled();

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
    }

    @Override
    public void requestLocationUpdates() {
        if (!backgroundLocationUpdatesEnabled) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,"not requesting location updates since background location updates is not enabled");
            return;
        }

        if (locationFetchMode == CTGeofenceSettings.FETCH_AUTO) {
            fusedProviderClient.requestLocationUpdates(getLocationRequest(),getLocationPendingIntent());
        }
    }

    @Override
    public void removeLocationUpdates() {

    }

    @Override
    public void setLocationAccuracy(int accuracy) {

    }

    @Override
    public int getLocationAccuracy() {
        return 0;
    }

    protected LocationRequest getLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(INTERVAL_IN_MILLIS);
        locationRequest.setFastestInterval(INTERVAL_FASTEST_IN_MILLIS);
        locationRequest.setPriority(locationAccuracy);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT_IN_METERS);

        return locationRequest;
    }

    private PendingIntent getLocationPendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (locationPendingIntent != null) {
            return locationPendingIntent;
        }
        Intent intent = new Intent(context.getApplicationContext(), CTLocationUpdateReceiver.class);
        // We use FLAG_NO_CREATE so that we get the same pending intent back if it exists when
        // calling requestLocationUpdates()
        locationPendingIntent = PendingIntent.getBroadcast(context, 102, intent, PendingIntent.
                FLAG_NO_CREATE);

        return locationPendingIntent;
    }

}
