package com.clevertap.android.geofence.interfaces;

import com.clevertap.android.geofence.model.CTGeofence;

import java.util.List;

public interface CTGeofenceRepository {

   void addGeofence(CTGeofence fence);
   void addAllGeofence(List<CTGeofence> fenceList);
   void removeGeofence(String id);
   void removeAllGeofence();


}
