package com.clevertap.android.geofence;

import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.NonNull;

import com.clevertap.android.geofence.interfaces.CTGeofenceAdapter;
import com.clevertap.android.geofence.model.CTGeofence;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class GoogleGeofenceAdapter implements CTGeofenceAdapter {

    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = Geofence.NEVER_EXPIRE;
    private final Context context;
    private final GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    GoogleGeofenceAdapter(Context context) {
        this.context = context;
        geofencingClient = LocationServices.getGeofencingClient(context);
    }

    @Override
    public void addGeofence(CTGeofence fence) {

    }

    @Override
    public void addAllGeofence(List<CTGeofence> fenceList) {

        if (fenceList == null || fenceList.isEmpty()) {
            return;
        }

        ArrayList<Geofence> googleFenceList = getGoogleGeofences(fenceList);

        try {
            // should get same pendingIntent on each app launch or else instance will leak
            PendingIntent geofencePendingIntent = PendingIntentFactory.getPendingIntent(context,
                    PendingIntentFactory.PENDING_INTENT_GEOFENCE, FLAG_UPDATE_CURRENT);

            geofencingClient.addGeofences(getGeofencingRequest(googleFenceList), geofencePendingIntent)
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
                            e.printStackTrace();
                        }
                    });

        } catch (Exception e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Failed to add geofences for monitoring");
            e.printStackTrace();
        }

    }


    @Override
    public void removeGeofence(String id) {

    }

    @Override
    public void removeAllGeofence(List<String> fenceIdList) {

        if (fenceIdList == null || fenceIdList.isEmpty()) {
            return;
        }

        try {
            geofencingClient.removeGeofences(fenceIdList)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Geofence removed successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Failed to remove registered geofences");
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Failed to remove registered geofences");
            e.printStackTrace();
        }
    }

    @Override
    public void stopGeofenceMonitoring(final PendingIntent pendingIntent) {

        if (pendingIntent == null) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Can't stop geofence monitoring since provided pendingIntent is null");
            return;
        }

        try {
            geofencingClient.removeGeofences(pendingIntent)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Geofence removed successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Failed to remove registered geofences");
                            e.printStackTrace();
                        }
                    });

            // cancel pending intent when no further updates required
            pendingIntent.cancel();

        } catch (Exception e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Failed to remove registered geofences");
            e.printStackTrace();
        }
    }

    private GeofencingRequest getGeofencingRequest(ArrayList<Geofence> googleFenceList) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(googleFenceList)
                .build();
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
