package com.clevertap.android.geofence.interfaces;

public interface CTLocationRepository {
    void requestLocationUpdates();
    void removeLocationUpdates();
    void setLocationAccuracy(int accuracy);
    int getLocationAccuracy();
}
