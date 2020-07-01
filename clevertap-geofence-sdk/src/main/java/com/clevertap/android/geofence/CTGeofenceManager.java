package com.clevertap.android.geofence;

import android.content.Context;

import com.clevertap.android.geofence.interfaces.CTGeofenceRepository;
import com.clevertap.android.geofence.model.CTGeofence;

import java.util.List;

class CTGeofenceManager implements CTGeofenceRepository {

    private final Context context;
    private CTGeofenceRepository geofenceRepository;

    CTGeofenceManager(Context context) {
        this.context = context;
    }

    void setGeofenceRepository(CTGeofenceRepository repository)
    {
        geofenceRepository=repository;
    }

    public CTGeofenceRepository getGeofenceRepository() {
        return geofenceRepository;
    }

    @Override
    public void addGeofence(CTGeofence fence) {

    }

    @Override
    public void addAllGeofence(List<CTGeofence> fenceList) {
        geofenceRepository.addAllGeofence(fenceList);
    }

    @Override
    public void removeGeofence(String id) {

    }

    @Override
    public void removeAllGeofence() {

    }
}
