package com.clevertap.android.geofence;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.clevertap.android.geofence.interfaces.CTGeofenceTask;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Future;

public class PushGeofenceEventTask implements CTGeofenceTask {

    private final Context context;
    private final Intent intent;
    private OnCompleteListener onCompleteListener;

    PushGeofenceEventTask(Context context, Intent intent) {
        this.context = context.getApplicationContext();
        this.intent = intent;
    }


    @Override
    public void execute() {

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Executing PushGeofenceEventTask...");

        if (!Utils.initCTGeofenceApiIfRequired(context.getApplicationContext())) {
            // if init fails then return without doing any work
            sendOnCompleteEvent();
            return;
        }


        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        //TODO Do we need null check?
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "error while processing geofence event: " + errorMessage);
            sendOnCompleteEvent();
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

            pushGeofenceEvents(triggeringGeofences, triggeringLocation, geofenceTransition);

        } else {
            // Log the error.
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "invalid geofence transition type: " + geofenceTransition);
        }

        sendOnCompleteEvent();

    }

    private void sendOnCompleteEvent() {
        if (onCompleteListener != null) {
            onCompleteListener.onComplete();
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
        String oldFenceListString = FileUtils.readFromFile(context,
                FileUtils.getCachedFullPath(context, CTGeofenceConstants.CACHED_FILE_NAME));
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

                            Future<?> future;

                            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                                future = CTGeofenceAPI.getInstance(context).getCleverTapApi()
                                        .pushGeofenceEnteredEvent(geofence);
                                if(CTGeofenceAPI.getInstance(context).getCtGeofenceEventsListener() != null) {
                                    CTGeofenceAPI.getInstance(context)
                                            .getCtGeofenceEventsListener()
                                            .onGeofenceEnteredEvent(geofence);
                                }
                            } else {
                                future = CTGeofenceAPI.getInstance(context).getCleverTapApi()
                                        .pushGeoFenceExitedEvent(geofence);
                                if(CTGeofenceAPI.getInstance(context).getCtGeofenceEventsListener() != null) {
                                    CTGeofenceAPI.getInstance(context)
                                            .getCtGeofenceEventsListener()
                                            .onGeofenceExitedEvent(geofence);
                                }
                            }

                            try {
                                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                        "Calling future for geofence event with id = " +
                                                triggeredGeofence.getRequestId());
                                future.get();

                                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                        "Finished calling future for geofence event with id = " +
                                                triggeredGeofence.getRequestId());
                            } catch (Exception e)
                            {
                                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                        "Failed to push geofence event with id = " +
                                                triggeredGeofence.getRequestId());
                                e.printStackTrace();
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
    }

    @Override
    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }
}
