package com.clevertap.android.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

class PendingIntentFactory {

    static final int PENDING_INTENT_LOCATION = 1;
    static final int PENDING_INTENT_GEOFENCE = 2;


    static PendingIntent getPendingIntent(Context context, int pendingIntentType, int flags) {

        int boradcastSenderRequestCode = 0;
        Intent intent = null;

        switch (pendingIntentType) {
            case PENDING_INTENT_LOCATION:
                intent = new Intent(context, CTLocationUpdateReceiver.class);
                intent.setAction(CTGeofenceConstants.ACTION_LOCATION_RECEIVER);
                boradcastSenderRequestCode = 102;
                break;
            case PENDING_INTENT_GEOFENCE:
                intent = new Intent(context, CTGeofenceBroadcastReceiver.class);
                intent.setAction(CTGeofenceConstants.ACTION_GEOFENCE_RECEIVER);
                boradcastSenderRequestCode = 100;
                break;
        }

        return PendingIntent.getBroadcast(context, boradcastSenderRequestCode, intent, flags);

    }

}
