package com.clevertap.android.geofence;

import com.clevertap.android.geofence.interfaces.CTLocationRepository;

public class AndroidLocationRepository implements CTLocationRepository {
    @Override
    public void requestLocationUpdates() {

    }

    @Override
    public void removeLocationUpdates() {

    }

    @Override
    public void setLocationAccuracy(int accuracy) {

    }

    @Override
    public int getLocationAccuracy() {
        return 0;
    }
}
