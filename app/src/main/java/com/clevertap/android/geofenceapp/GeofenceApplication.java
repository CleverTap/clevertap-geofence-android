package com.clevertap.android.geofenceapp;

import android.app.Application;

import com.clevertap.android.geofence.Logger;
import com.clevertap.android.geofence.model.CTGeofenceSettings;
import com.clevertap.android.sdk.ActivityLifecycleCallback;
import com.clevertap.android.sdk.CleverTapAPI;

public class GeofenceApplication extends Application {

    @Override
    public void onCreate() {

        ActivityLifecycleCallback.register(this);
        super.onCreate();

        CleverTapAPI clevertapDefaultInstance = CleverTapAPI.getDefaultInstance(this);
        CleverTapAPI.setDebugLevel(10);

        clevertapDefaultInstance.initGeofenceAPI(new CTGeofenceSettings.Builder()
                .enableBackgroundLocationUpdates(true)
                .setDebugLevel(Logger.LogLevel.DEBUG)
                .setLocationAccuracy(CTGeofenceSettings.ACCURACY_HIGH)
                .setLocationFetchMode(CTGeofenceSettings.FETCH_CURRENT_LOCATION_PERIODIC)
                .build());

    }
}
