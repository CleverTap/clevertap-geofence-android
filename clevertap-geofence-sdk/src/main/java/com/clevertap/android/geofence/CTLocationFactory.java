package com.clevertap.android.geofence;

import android.content.Context;

import com.clevertap.android.geofence.interfaces.CTLocationAdapter;

 class CTLocationFactory {

    static CTLocationAdapter createLocationAdapter(Context context){

        if (Utils.isFusedLocationApiDependencyAvailable()){
            return new GoogleLocationAdapter(context);
        }else {
            return new AndroidLocationAdapter(context);
        }
    }
}
