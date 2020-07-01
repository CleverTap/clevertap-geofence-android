package com.clevertap.android.geofence;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import com.clevertap.android.geofence.interfaces.CTGeofenceRepository;
import com.clevertap.android.geofence.model.CTGeofence;
import com.google.android.gms.location.Geofence;

import java.security.Permission;
import java.util.List;

public class AndroidGeofenceRepository implements CTGeofenceRepository {

    private final Context context;
    private final LocationManager locationManager;
    private final String ACTION_PROXIMITY_ALERT = "com.clevertap.android.geofence.proximity";
    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = -1;


    AndroidGeofenceRepository(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void addGeofence(CTGeofence fence) {

    }

    @SuppressLint("MissingPermission")
    @Override
    public void addAllGeofence(List<CTGeofence> fenceList) {
        Intent intent = new Intent(ACTION_PROXIMITY_ALERT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(),
                100, intent, 0);

        if (Utils.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            for (CTGeofence ctGeofence : fenceList) {

                locationManager.addProximityAlert(ctGeofence.getLatitude(), ctGeofence.getLongitude(),
                        ctGeofence.getRadius(), GEOFENCE_EXPIRATION_IN_MILLISECONDS, pendingIntent);
            }

        }
    }

    @Override
    public void removeGeofence(String id) {

    }

    @Override
    public void removeAllGeofence() {

    }
}
