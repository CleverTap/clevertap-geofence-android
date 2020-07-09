package com.clevertap.android.geofence.interfaces;

import android.app.PendingIntent;

import com.clevertap.android.geofence.model.CTGeofence;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public interface CTGeofenceAdapter {

   void addGeofence(CTGeofence fence);
   void addAllGeofence(List<CTGeofence> fenceList, OnSuccessListener onSuccessListener);
   void removeGeofence(String id);
   void removeAllGeofence(List<String> fenceIdList, OnSuccessListener onSuccessListener);
   void stopGeofenceMonitoring(PendingIntent pendingIntent);


}
