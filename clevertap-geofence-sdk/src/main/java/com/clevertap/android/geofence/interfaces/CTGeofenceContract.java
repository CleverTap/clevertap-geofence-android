package com.clevertap.android.geofence.interfaces;

import android.location.Location;

import org.json.JSONObject;

public interface CTGeofenceContract {

    void setLocation(Location location);
    void geofenceTransitionHit(JSONObject object);
    void setGeoFenceCallback(CTGeofenceCallback callback);

}
