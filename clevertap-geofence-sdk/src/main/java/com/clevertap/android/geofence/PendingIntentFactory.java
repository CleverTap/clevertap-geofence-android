package com.clevertap.android.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

class PendingIntentFactory {

    static final int PENDING_INTENT_LOCATION = 1;
    static final int PENDING_INTENT_GEOFENCE = 2;

    @Nullable
    static PendingIntent getPendingIntent(@Nullable Context context, int pendingIntentType, int flags) {

        if (context == null)
            return null;

        int broadcastSenderRequestCode;
        Intent intent;

        switch (pendingIntentType) {
            case PENDING_INTENT_LOCATION:
                intent = new Intent(context.getApplicationContext(), CTLocationUpdateReceiver.class);
                intent.setAction(CTGeofenceConstants.ACTION_LOCATION_RECEIVER);
                broadcastSenderRequestCode = 10100111;
                break;
            case PENDING_INTENT_GEOFENCE:
                intent = new Intent(context.getApplicationContext(), CTGeofenceReceiver.class);
                intent.setAction(CTGeofenceConstants.ACTION_GEOFENCE_RECEIVER);
                broadcastSenderRequestCode = 1001001;
                break;
            default:
                throw new IllegalArgumentException("invalid pendingIntentType");
        }

        return PendingIntent.getBroadcast(context.getApplicationContext(), broadcastSenderRequestCode, intent, flags);

    }

}
