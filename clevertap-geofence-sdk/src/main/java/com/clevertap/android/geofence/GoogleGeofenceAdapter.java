package com.clevertap.android.geofence;

import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

class GoogleGeofenceAdapter implements CTGeofenceAdapter {

    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = Geofence.NEVER_EXPIRE;
    private final Context context;
    private final GeofencingClient geofencingClient;

    GoogleGeofenceAdapter(@NonNull Context context) {
        this.context = context.getApplicationContext();
        geofencingClient = LocationServices.getGeofencingClient(this.context);
    }

    @SuppressWarnings("unchecked")
    @WorkerThread
    @Override
    public void addAllGeofence(@Nullable List<CTGeofence> fenceList, @NonNull final OnSuccessListener onSuccessListener) {

        if (fenceList == null || fenceList.isEmpty()) {
            return;
        }

        ArrayList<Geofence> googleFenceList = getGoogleGeofences(fenceList);
        Void aVoid = null;

        try {
            // should get same pendingIntent on each app launch or else instance will leak
            PendingIntent geofencePendingIntent = PendingIntentFactory.getPendingIntent(context,
                    PendingIntentFactory.PENDING_INTENT_GEOFENCE, FLAG_UPDATE_CURRENT);

            Task<Void> addGeofenceTask = geofencingClient.addGeofences(getGeofencingRequest(googleFenceList), geofencePendingIntent);
            // blocking task
            aVoid = Tasks.await(addGeofenceTask);
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Geofence registered successfully");

        } catch (Exception e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Failed to add geofences for monitoring");
            e.printStackTrace();
        } finally {
            onSuccessListener.onSuccess(aVoid);
        }

    }


    @SuppressWarnings("unchecked")
    @WorkerThread
    @Override
    public void removeAllGeofence(@Nullable List<String> fenceIdList, @NonNull final OnSuccessListener onSuccessListener) {

        if (fenceIdList == null || fenceIdList.isEmpty()) {
            return;
        }

        Void aVoid = null;
        try {
            Task<Void> removeGeofenceTask = geofencingClient.removeGeofences(fenceIdList);
            // blocking task
            aVoid = Tasks.await(removeGeofenceTask);
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Geofence removed successfully");

        } catch (Exception e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Failed to remove registered geofences");
            e.printStackTrace();
        } finally {
            onSuccessListener.onSuccess(aVoid);
        }
    }

    @WorkerThread
    @Override
    public void stopGeofenceMonitoring(@Nullable final PendingIntent pendingIntent) {

        if (pendingIntent == null) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Can't stop geofence monitoring since provided pendingIntent is null");
            return;
        }

        try {
            Task<Void> removeGeofenceTask = geofencingClient.removeGeofences(pendingIntent);

            // blocking task
            Tasks.await(removeGeofenceTask);

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

    @NonNull
    private ArrayList<Geofence> getGoogleGeofences(@NonNull List<CTGeofence> fenceList) {
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
