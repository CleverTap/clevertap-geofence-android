package com.clevertap.android.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.clevertap.android.geofence.interfaces.CTGeofenceRepository;
import com.clevertap.android.geofence.model.CTGeofence;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class GoogleGeofenceRepository implements CTGeofenceRepository {

    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = Geofence.NEVER_EXPIRE;
    private final Context context;
    private final GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    GoogleGeofenceRepository(Context context) {
        this.context = context;
        geofencingClient = LocationServices.getGeofencingClient(context);
    }

    @Override
    public void addGeofence(CTGeofence fence) {

    }

    @Override
    public void addAllGeofence(List<CTGeofence> fenceList) {

        ArrayList<Geofence> googleFenceList = getGoogleGeofences(fenceList);

        geofencingClient.addGeofences(getGeofencingRequest(googleFenceList), getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Geofence registered successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Geofence failed to register");
                    }
                });

    }


    @Override
    public void removeGeofence(String id) {

    }

    @Override
    public void removeAllGeofence() {

    }

    private GeofencingRequest getGeofencingRequest(ArrayList<Geofence> googleFenceList) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(googleFenceList)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(context.getApplicationContext(), CTGeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(context, 100, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);

        return geofencePendingIntent;
    }


    private ArrayList<Geofence> getGoogleGeofences(List<CTGeofence> fenceList) {
        ArrayList<Geofence> googleFenceList = new ArrayList<>();

        for (CTGeofence ctGeofence : fenceList) {
            googleFenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(ctGeofence.getId())

                    .setCircularRegion(ctGeofence.getLatitude(), ctGeofence.getLongitude(),
                            ctGeofence.getRadius())
                    .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }
        return googleFenceList;
    }
}
