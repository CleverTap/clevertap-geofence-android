package com.clevertap.android.geofence.interfaces;

import android.location.Location;

import org.json.JSONObject;

public interface CTGeofenceInterface {

    void setLocationForGeofences(Location location);
    void pushGeofenceEnteredEvent(JSONObject object);
    void pushGeoFenceExitedEvent(JSONObject object);
    void setGeoFenceCallback(CTGeofenceCallback callback);

}
