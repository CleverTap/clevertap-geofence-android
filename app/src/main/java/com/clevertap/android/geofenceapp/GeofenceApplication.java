package com.clevertap.android.geofenceapp;

import android.app.Application;

import com.clevertap.android.geofence.CTGeofenceAPI;
import com.clevertap.android.geofence.CTGeofenceSettings;
import com.clevertap.android.geofence.Logger;
import com.clevertap.android.sdk.ActivityLifecycleCallback;
import com.clevertap.android.sdk.CleverTapAPI;

import org.json.JSONObject;

public class GeofenceApplication extends Application {

    @Override
    public void onCreate() {

        ActivityLifecycleCallback.register(this);
        super.onCreate();

        CleverTapAPI clevertapDefaultInstance = CleverTapAPI.getDefaultInstance(this);
        CleverTapAPI.setDebugLevel(10);

        CTGeofenceAPI.getInstance(getApplicationContext())
                .init(new CTGeofenceSettings.Builder()
                        .enableBackgroundLocationUpdates(true)
                        .setLogLevel(Logger.DEBUG)
                        .setLocationAccuracy(CTGeofenceSettings.ACCURACY_HIGH)
                        .setLocationFetchMode(CTGeofenceSettings.FETCH_CURRENT_LOCATION_PERIODIC)
                        .build(),clevertapDefaultInstance);


    }
}
