package com.clevertap.android.geofence;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.work.ListenableWorker;

import com.clevertap.android.sdk.CleverTapAPI;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class CTGeofenceService extends JobIntentService {

    /**
     * Unique job ID for this service, must be the same value for all work
     * enqueued for the same class.
     */
    static final int JOB_ID = 1010; //TODO use accountId.hashCode + some fixed number as JobId

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context.getApplicationContext(), CTGeofenceService.class, JOB_ID, work);
    }


    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Handling work in CTGeofenceService...");

        if (!Utils.initCTGeofenceApiIfRequired(getApplicationContext()))
        {
            // if init fails then return without doing any work
            return;
        }


        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        //TODO Do we need null check?
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "error while processing geofence event: " + errorMessage);
            return;
        }

        // Get the transition type.
        final int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {


            // Get the geofences that were triggered. A single event can trigger multiple geofences.

            final List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            final Location triggeringLocation = geofencingEvent.getTriggeringLocation();

            // send geofence event through queue to avoid loss of old geofence data
            // for example. while searching triggered geofence in file, it may be overwritten by new fences received from server
            CTGeofenceTaskManager.getInstance().postAsyncSafely("PushGeofenceEvent",
                    new Runnable() {
                        @Override
                        public void run() {
                            pushGeofenceEvents(triggeringGeofences, triggeringLocation, geofenceTransition);
                        }
                    });

        } else {
            // Log the error.
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "invalid geofence transition type: " + geofenceTransition);
        }
    }


    /**
     * Push geofence event to CT SDK. If multiple geofences are triggered then send it sequentially
     *
     * @param triggeringGeofences - List of {@link Geofence}
     * @param triggeringLocation  - {@link Location} object
     * @param geofenceTransition  - int value of geofence transition event
     */
    private void pushGeofenceEvents(List<Geofence> triggeringGeofences, Location triggeringLocation,
                                    int geofenceTransition) {

        //TODO: Finalize contract for geofence trigger event
        if (triggeringGeofences == null) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "fetched triggered geofence list is null");
            return;

        }

        // Search triggered geofences in file by id and send stored geofence object to CT SDK
        String oldFenceListString = FileUtils.readFromFile(getApplicationContext(),
                FileUtils.getCachedFullPath(getApplicationContext(), CTGeofenceConstants.CACHED_FILE_NAME));
        if (oldFenceListString != null && !oldFenceListString.trim().equals("")) {

            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(oldFenceListString);
                JSONArray jsonArray = jsonObject.getJSONArray(CTGeofenceConstants.KEY_GEOFENCES);

                for (Geofence triggeredGeofence : triggeringGeofences) {

                    CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                            "Searching Triggered geofence with id = " + triggeredGeofence.getRequestId()
                                    + " in file...");

                    boolean isTriggeredGeofenceFound = false;

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject geofence = jsonArray.getJSONObject(i);
                        if (String.valueOf(geofence.getInt(CTGeofenceConstants.KEY_ID))
                                .equals(triggeredGeofence.getRequestId())) {
                            // triggered geofence found in file

                            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                    "Triggered geofence with id = " + triggeredGeofence.getRequestId()
                                            + " is found in file! Sending it to CT SDK");

                            isTriggeredGeofenceFound = true;

                            geofence.put("triggered_lat", triggeringLocation.getLatitude());
                            geofence.put("triggered_lng", triggeringLocation.getLongitude());

                            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                                CTGeofenceAPI.getInstance(getApplicationContext()).getGeofenceInterface()
                                        .pushGeofenceEnteredEvent(geofence);
                            } else {
                                CTGeofenceAPI.getInstance(getApplicationContext()).getGeofenceInterface()
                                        .pushGeoFenceExitedEvent(geofence);
                            }

                            break;
                            //TODO add some verbose logging here to help us debug prod issues
                        }
                    }

                    if (!isTriggeredGeofenceFound) {
                        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                "Triggered geofence with id = " + triggeredGeofence.getRequestId()
                                        + " is not found in file! Dropping this event");
                    }

                }
            } catch (Exception e) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Failed to read triggered geofences from file");
                e.printStackTrace();
            }
        }

        // due to some reasons if triggered fences is not found then push below response
        /*if (!isTriggeredGeofenceFound) {

            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            for (Geofence geofence : triggeringGeofences) {
                jsonArray.put(geofence.getRequestId());

            }
            try {
                if (triggeringLocation != null) {
                    jsonObject.put("triggered_lat", triggeringLocation.getLatitude());
                    jsonObject.put("triggered_lng", triggeringLocation.getLongitude());
                }
                jsonObject.put("triggered_fences", jsonArray);

                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    CTGeofenceAPI.getInstance(getApplicationContext()).getGeofenceInterface()
                            .pushGeofenceEnteredEvent(jsonObject);
                } else {
                    CTGeofenceAPI.getInstance(getApplicationContext()).getGeofenceInterface()
                            .pushGeoFenceExitedEvent(jsonObject);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }*/

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "destroying geofence service..");
    }
}
