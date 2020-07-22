package com.clevertap.android.geofence.interfaces;

import android.location.Location;

import org.json.JSONObject;

import java.util.concurrent.Future;

public interface CTGeofenceInterface {

    Future<?> setLocationForGeofences(Location location, int sdkVersion);
    Future<?> pushGeofenceEnteredEvent(JSONObject object);
    Future<?> pushGeoFenceExitedEvent(JSONObject object);
    void setGeoFenceCallback(CTGeofenceCallback callback);

}
