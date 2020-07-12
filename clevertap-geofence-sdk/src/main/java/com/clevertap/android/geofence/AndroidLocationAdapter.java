package com.clevertap.android.geofence;

import android.app.PendingIntent;
import android.content.Context;

import com.clevertap.android.geofence.interfaces.CTLocationCallback;
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
    public void getLastLocation(CTLocationCallback callback) {
    }
}
