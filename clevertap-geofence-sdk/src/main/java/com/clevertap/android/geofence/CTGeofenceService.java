package com.clevertap.android.geofence;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CTGeofenceService extends JobIntentService {

    /**
     * Unique job ID for this service, must be the same value for all work
     * enqueued for the same class.
     */
    static final int JOB_ID = 1010;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, CTGeofenceService.class, JOB_ID, work);
    }


    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent == null || geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "error while processing geofence event: " + errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {


            // Get the geofences that were triggered. A single event can trigger multiple geofences.

            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            Location triggeringLocation = geofencingEvent.getTriggeringLocation();

            JSONObject jsonObject = toJsonObject(triggeringGeofences, triggeringLocation);

            CTGeofenceAPI.getInstance(getApplicationContext()).getGeofenceContract()
                    .geofenceTransitionHit(jsonObject);


        } else {
            // Log the error.
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "invalid geofence transition type: " + geofenceTransition);
        }
    }

    private JSONObject toJsonObject(List<Geofence> triggeringGeofences, Location triggeringLocation) {

        if (triggeringGeofences == null) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "fetched triggered geofence list is null");
            return null;

        }
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        for (Geofence geofence : triggeringGeofences) {
            jsonArray.put(geofence.getRequestId());

        }
        try {
            if (triggeringLocation != null) {
                jsonObject.put("latitude", triggeringLocation.getLatitude());
                jsonObject.put("longitude", triggeringLocation.getLongitude());
            }
            jsonObject.put("triggered_fences", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "destroying geofence service..");
    }
}
