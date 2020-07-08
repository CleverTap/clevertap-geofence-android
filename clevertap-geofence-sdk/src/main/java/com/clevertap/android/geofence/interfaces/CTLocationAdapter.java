package com.clevertap.android.geofence.interfaces;

import android.app.PendingIntent;

public interface CTLocationAdapter {
    void requestLocationUpdates();
    void removeLocationUpdates(PendingIntent pendingIntent);
    void setLocationAccuracy(int accuracy);
    void getLastLocation(CTLocatioCallback callback);
    int getLocationAccuracy();
}
