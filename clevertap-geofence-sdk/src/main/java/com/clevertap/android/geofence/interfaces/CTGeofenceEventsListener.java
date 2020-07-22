package com.clevertap.android.geofence.interfaces;

import org.json.JSONObject;

public interface CTGeofenceEventsListener {
    void onGeofenceEnteredEvent(JSONObject geofenceEnteredEventProperties);
    void onGeofenceExitedEvent(JSONObject geofenceExitedEventProperties);
}
