package com.clevertap.android.geofence;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.clevertap.android.geofence.interfaces.CTGeofenceEventsListener;
import com.clevertap.android.geofence.interfaces.CTGeofenceTask;
import com.clevertap.android.sdk.CleverTapAPI;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Future;

class PushGeofenceEventTask implements CTGeofenceTask {

    private final Context context;
    @NonNull private final Intent intent;
    @Nullable
    private OnCompleteListener onCompleteListener;

    PushGeofenceEventTask(Context context, @NonNull Intent intent) {
        this.context = context.getApplicationContext();
        this.intent = intent;
    }

    @WorkerThread
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

        CleverTapAPI cleverTapApi = CTGeofenceAPI.getInstance(context).getCleverTapApi();

        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "error while processing geofence event: " + errorMessage);
            if(cleverTapApi != null){
                cleverTapApi.pushGeoFenceError(CTGeofenceConstants.ERROR_CODE,
                                "error while processing geofence event: " + errorMessage);
            }
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
            if(cleverTapApi != null){
                cleverTapApi.pushGeoFenceError(CTGeofenceConstants.ERROR_CODE,
                                "invalid geofence transition type: " + geofenceTransition);
            }
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
    @WorkerThread
    private void pushGeofenceEvents(@Nullable List<Geofence> triggeringGeofences, @Nullable Location triggeringLocation,
                                    int geofenceTransition) {

        if (triggeringGeofences == null) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "fetched triggered geofence list is null");
            if(CTGeofenceAPI.getInstance(context).getCleverTapApi() != null){
                CTGeofenceAPI.getInstance(context)
                        .getCleverTapApi()
                        .pushGeoFenceError(CTGeofenceConstants.ERROR_CODE,
                                "fetched triggered geofence list is null");
            }
            return;

        }

        // Search triggered geofences in file by id and send stored geofence object to CT SDK
        String oldFenceListString = FileUtils.readFromFile(context,
                FileUtils.getCachedFullPath(context, CTGeofenceConstants.CACHED_FILE_NAME));
        if (!oldFenceListString.trim().equals("")) {

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
                        final JSONObject geofence = jsonArray.getJSONObject(i);
                        if (String.valueOf(geofence.getInt(CTGeofenceConstants.KEY_ID))
                                .equals(triggeredGeofence.getRequestId())) {
                            // triggered geofence found in file

                            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                    "Triggered geofence with id = " + triggeredGeofence.getRequestId()
                                            + " is found in file! Sending it to CT SDK");

                            isTriggeredGeofenceFound = true;

                            if (triggeringLocation != null) {
                                geofence.put("triggered_lat", triggeringLocation.getLatitude());
                                geofence.put("triggered_lng", triggeringLocation.getLongitude());
                            }

                            Future<?> future;

                            CleverTapAPI cleverTapApi = CTGeofenceAPI.getInstance(context).getCleverTapApi();

                            if (cleverTapApi == null) {
                                return;
                            }

                            final CTGeofenceEventsListener ctGeofenceEventsListener = CTGeofenceAPI
                                    .getInstance(context).getCtGeofenceEventsListener();

                            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                                future = cleverTapApi.pushGeofenceEnteredEvent(geofence);

                                if (ctGeofenceEventsListener != null) {
                                    com.clevertap.android.sdk.Utils.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ctGeofenceEventsListener.onGeofenceEnteredEvent(geofence);
                                        }
                                    });
                                }

                            } else {

                                future = cleverTapApi.pushGeoFenceExitedEvent(geofence);

                                if (ctGeofenceEventsListener != null) {
                                    com.clevertap.android.sdk.Utils.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ctGeofenceEventsListener.onGeofenceExitedEvent(geofence);
                                        }
                                    });
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
                            } catch (Exception e) {
                                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                        "Failed to push geofence event with id = " +
                                                triggeredGeofence.getRequestId());
                                e.printStackTrace();
                            }

                            break;
                        }
                    }

                    if (!isTriggeredGeofenceFound) {
                        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                "Triggered geofence with id = " + triggeredGeofence.getRequestId()
                                        + " is not found in file! Dropping this event");
                        if(CTGeofenceAPI.getInstance(context).getCleverTapApi() != null){
                            CTGeofenceAPI.getInstance(context)
                                    .getCleverTapApi()
                                    .pushGeoFenceError(CTGeofenceConstants.ERROR_CODE,
                                            "Triggered geofence with id = " +
                                                    triggeredGeofence.getRequestId()
                                                    + " is not found in file! Dropping this event");
                        }
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
    public void setOnCompleteListener(@NonNull OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }
}
