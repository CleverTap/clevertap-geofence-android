package com.clevertap.android.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

class PendingIntentFactory {

    static final int PENDING_INTENT_LOCATION = 1;
    static final int PENDING_INTENT_GEOFENCE = 2;


    static PendingIntent getPendingIntent(Context context, int pendingIntentType, int flags) {

        int broadcastSenderRequestCode;
        Intent intent;

        switch (pendingIntentType) {
            case PENDING_INTENT_LOCATION:
                intent = new Intent(context, CTLocationUpdateReceiver.class);
                intent.setAction(CTGeofenceConstants.ACTION_LOCATION_RECEIVER);
                broadcastSenderRequestCode = 102;
                break;
            case PENDING_INTENT_GEOFENCE:
                intent = new Intent(context, CTGeofenceReceiver.class);
                intent.setAction(CTGeofenceConstants.ACTION_GEOFENCE_RECEIVER);
                broadcastSenderRequestCode = 100;
                break;
            default:
                throw new IllegalArgumentException("invalid pendingIntentType");
        }

        return PendingIntent.getBroadcast(context, broadcastSenderRequestCode, intent, flags);

    }

}
