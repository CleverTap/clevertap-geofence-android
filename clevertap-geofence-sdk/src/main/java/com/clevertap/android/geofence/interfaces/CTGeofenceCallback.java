package com.clevertap.android.geofence.interfaces;

import org.json.JSONObject;

public interface CTGeofenceCallback {

    void onSuccess(JSONObject fenceList);

    void onFailure(Throwable error);

}
