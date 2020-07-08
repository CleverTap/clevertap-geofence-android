package com.clevertap.android.geofence.interfaces;

import android.app.PendingIntent;

import com.clevertap.android.geofence.model.CTGeofence;

import java.util.List;

public interface CTGeofenceAdapter {

   void addGeofence(CTGeofence fence);
   void addAllGeofence(List<CTGeofence> fenceList);
   void removeGeofence(String id);
   void removeAllGeofence(List<String> fenceIdList);
   void stopGeofenceMonitoring(PendingIntent pendingIntent);


}
