package com.clevertap.android.geofenceapp;

import android.app.Application;

import com.clevertap.android.sdk.ActivityLifecycleCallback;

public class GeofenceApplication extends Application {

    @Override
    public void onCreate() {
        ActivityLifecycleCallback.register(this);
        super.onCreate();
    }
}
