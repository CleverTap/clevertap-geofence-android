package com.clevertap.android.geofence.interfaces;

public interface CTGeofenceTask {

    void execute();
    void setOnCompleteListener(OnCompleteListener onCompleteListener);
    interface OnCompleteListener {
        void onComplete();
    }
}
