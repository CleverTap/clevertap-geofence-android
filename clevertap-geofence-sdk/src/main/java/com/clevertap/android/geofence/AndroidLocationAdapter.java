package com.clevertap.android.geofence;

import android.app.PendingIntent;
import android.content.Context;

import com.clevertap.android.geofence.interfaces.CTLocatioCallback;
import com.clevertap.android.geofence.interfaces.CTLocationAdapter;

public class AndroidLocationAdapter implements CTLocationAdapter {
    public AndroidLocationAdapter(Context context) {

    }

    @Override
    public void requestLocationUpdates() {

    }

    @Override
    public void removeLocationUpdates(PendingIntent pendingIntent) {

    }

    @Override
    public void setLocationAccuracy(int accuracy) {

    }

    @Override
    public int getLocationAccuracy() {
        return 0;
    }

    @Override
    public void getLastLocation(CTLocatioCallback callback) {
    }
}
