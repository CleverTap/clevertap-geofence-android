package com.clevertap.android.geofence;

import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.WorkerThread;

import com.clevertap.android.geofence.interfaces.CTGeofenceAdapter;
import com.clevertap.android.geofence.model.CTGeofence;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class GoogleGeofenceAdapter implements CTGeofenceAdapter {

    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = Geofence.NEVER_EXPIRE;
    private final Context context;
    private final GeofencingClient geofencingClient;

    GoogleGeofenceAdapter(Context context) {
        this.context = context.getApplicationContext();
        geofencingClient = LocationServices.getGeofencingClient(this.context);
    }

    @SuppressWarnings("unchecked")
    @WorkerThread
    @Override
    public void addAllGeofence(List<CTGeofence> fenceList, final OnSuccessListener onSuccessListener) {

        if (fenceList == null || fenceList.isEmpty()) {
            return;
        }

        ArrayList<Geofence> googleFenceList = getGoogleGeofences(fenceList);

        try {
            // should get same pendingIntent on each app launch or else instance will leak
            PendingIntent geofencePendingIntent = PendingIntentFactory.getPendingIntent(context,
                    PendingIntentFactory.PENDING_INTENT_GEOFENCE, FLAG_UPDATE_CURRENT);

            Task<Void> addGeofenceTask = geofencingClient.addGeofences(getGeofencingRequest(googleFenceList), geofencePendingIntent);
            // blocking task
            Void aVoid = Tasks.await(addGeofenceTask);
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Geofence registered successfully");

            if (onSuccessListener != null) {
                onSuccessListener.onSuccess(aVoid);
            }

            /*addGeofenceTask.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // called on main thread
                    if (onSuccessListener != null) {
                        onSuccessListener.onSuccess(aVoid);
                    }
                    CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Geofence registered successfully");
                }
            });
            addGeofenceTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Geofence failed to register");
                    e.printStackTrace();
                }
            });*/

        } catch (Exception e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Failed to add geofences for monitoring");
            e.printStackTrace();
        }

    }


    @SuppressWarnings("unchecked")
    @WorkerThread
    @Override
    public void removeAllGeofence(List<String> fenceIdList, final OnSuccessListener onSuccessListener) {

        if (fenceIdList == null || fenceIdList.isEmpty()) {
            return;
        }

        try {
            Task<Void> removeGeofenceTask = geofencingClient.removeGeofences(fenceIdList);
            // blocking task
            Void aVoid = Tasks.await(removeGeofenceTask);
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Geofence removed successfully");

            if (onSuccessListener != null) {
                onSuccessListener.onSuccess(aVoid);
            }

            /*removeGeofenceTask.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (onSuccessListener != null) {
                        onSuccessListener.onSuccess(aVoid);
                    }
                    CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Geofence removed successfully");
                }
            });
            removeGeofenceTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Failed to remove registered geofences");
                    e.printStackTrace();
                }
            });*/
        } catch (Exception e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Failed to remove registered geofences");
            e.printStackTrace();
        }
    }

    @WorkerThread
    @Override
    public void stopGeofenceMonitoring(final PendingIntent pendingIntent) {

        if (pendingIntent == null) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Can't stop geofence monitoring since provided pendingIntent is null");
            return;
        }

        try {
            Task<Void> removeGeofenceTask = geofencingClient.removeGeofences(pendingIntent);

            // blocking task
            Tasks.await(removeGeofenceTask);

            /*removeGeofenceTask.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Geofence removed successfully");
                }
            });
            removeGeofenceTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Failed to remove registered geofences");
                    e.printStackTrace();
                }
            });*/

            // cancel pending intent when no further updates required
            pendingIntent.cancel();
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Geofence removed successfully");

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
