package com.clevertap.android.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;

import com.clevertap.android.geofence.interfaces.CTGeofenceAdapter;
import com.clevertap.android.geofence.interfaces.CTGeofenceCallback;
import com.clevertap.android.geofence.interfaces.CTGeofenceInterface;
import com.clevertap.android.geofence.interfaces.CTGeofenceTask;
import com.clevertap.android.geofence.interfaces.CTLocatioCallback;
import com.clevertap.android.geofence.interfaces.CTLocationAdapter;
import com.clevertap.android.geofence.model.CTGeofenceSettings;

import org.json.JSONObject;

import static android.app.PendingIntent.FLAG_NO_CREATE;

public class CTGeofenceAPI implements CTGeofenceCallback {

    public static final String GEOFENCE_LOG_TAG = "CTGeofence";
    private static CTGeofenceAPI ctGeofenceAPI;
    private final Context context;
    private static Logger logger;
    private final CTLocationAdapter ctLocationAdapter;
    private final CTGeofenceAdapter ctGeofenceAdapter;

    private CTGeofenceSettings ctGeofenceSettings;
    private CTGeofenceInterface ctGeofenceInterface;
    private boolean isActivated;
    private OnGeofenceApiInitializedListener onGeofenceApiInitializedListener;

    private CTGeofenceAPI(Context context) {
        this.context = context;
        logger = new Logger(Logger.LogLevel.DEBUG);
        ctLocationAdapter = CTLocationFactory.createLocationAdapter(context);
        ctGeofenceAdapter = CTGeofenceFactory.createGeofenceAdapter(context);
    }

    public static CTGeofenceAPI getInstance(Context context) {
        if (ctGeofenceAPI == null) {
            synchronized (CTGeofenceAPI.class) {
                if (ctGeofenceAPI == null) {
                    ctGeofenceAPI = new CTGeofenceAPI(context);
                }
            }
        }
        return ctGeofenceAPI;
    }

    /**
     * Set geofence API configuration settings, should be used by CT SDK only
     *
     * @param ctGeofenceSettings
     */
    public void setGeofenceSettings(CTGeofenceSettings ctGeofenceSettings) {
        this.ctGeofenceSettings = ctGeofenceSettings;
    }

    /**
     * register an implementation of CTGeofenceInterface, should be used by CT SDK only
     *
     * @param ctGeofenceInterface
     */
    public void setGeofenceInterface(CTGeofenceInterface ctGeofenceInterface) {
        this.ctGeofenceInterface = ctGeofenceInterface;
    }

    /**
     * Listener for Geofence SDK initialize
     *
     * @param onGeofenceApiInitializedListener
     */
    public void setOnGeofenceApiInitializedListener(OnGeofenceApiInitializedListener onGeofenceApiInitializedListener) {
        this.onGeofenceApiInitializedListener = onGeofenceApiInitializedListener;
    }

    /**
     * Initializes location update receivers and geofence monitoring on background thread by
     * reading config settings if provided or will use default settings.
     * Should be used by CT SDK only
     */
    public void activate() {

        if (isActivated) {
            logger.debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Geofence API already activated! dropping this call");
            return;
        }

        if (ctGeofenceInterface == null) {
            throw new IllegalStateException("setGeofenceContract() must be called before activate()");
        }

        if (ctGeofenceSettings == null) {
            ctGeofenceSettings = initDefaultConfig();
        }

        logger.setDebugLevel(ctGeofenceSettings.getLogLevel());


        ctGeofenceInterface.setGeoFenceCallback(this);
        logger.debug(GEOFENCE_LOG_TAG, "geofence callback registered");

        LocationUpdateTask locationUpdateTask = new LocationUpdateTask(context);
        locationUpdateTask.setOnCompleteListener(new CTGeofenceTask.OnCompleteListener() {
            @Override
            public void onComplete() {
                isActivated = true;

                if (onGeofenceApiInitializedListener != null) {
                    onGeofenceApiInitializedListener.OnGeofenceApiInitialized();
                }
            }
        });

        CTGeofenceTaskManager.getInstance().postAsyncSafely("IntitializeLocationUpdates",
                locationUpdateTask);

    }

    /**
     * unregisters geofences, location updates and cleanup all resources
     */
    public void deactivate() {


        //  TODO: clean up resources


        CTGeofenceTaskManager.getInstance().postAsyncSafely("DeactivateApi", new Runnable() {
            @Override
            public void run() {
                // stop geofence monitoring
                PendingIntent geofencePendingIntent = PendingIntentFactory.getPendingIntent(context,
                        PendingIntentFactory.PENDING_INTENT_GEOFENCE, FLAG_NO_CREATE);
                ctGeofenceAdapter.stopGeofenceMonitoring(geofencePendingIntent);

                // stop location updates
                PendingIntent locationPendingIntent = PendingIntentFactory.getPendingIntent(context,
                        PendingIntentFactory.PENDING_INTENT_LOCATION, FLAG_NO_CREATE);
                ctLocationAdapter.removeLocationUpdates(locationPendingIntent);

                isActivated = false;
            }
        });

    }

    /**
     * fetches last location from FusedLocationAPI and delivers the result on main thread
     */
    public void triggerLocation() {

        if (!isActivated) {
            throw new IllegalStateException("activate() must be called before triggerLocation()");
        }

        ctLocationAdapter.getLastLocation(new CTLocatioCallback() {
            @Override
            public void onLocationComplete(Location location) {
                //get's called on main thread
                ctGeofenceInterface.setLocationForGeofences(location);
            }
        });

    }

    private CTGeofenceSettings initDefaultConfig() {
        return new CTGeofenceSettings.Builder().build();
    }

    @Override
    public void onSuccess(final JSONObject fenceList) {

        if (fenceList == null) {
            logger.debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Geofence response is null! dropping further processing");
            return;
        }

        GeofenceUpdateTask geofenceUpdateTask = new GeofenceUpdateTask(context, fenceList);

        CTGeofenceTaskManager.getInstance().postAsyncSafely("ProcessGeofenceUpdates",
                geofenceUpdateTask);

    }

    @Override
    public void onFailure(Throwable error) {

    }


    CTGeofenceAdapter getCtGeofenceAdapter() {
        return ctGeofenceAdapter;
    }

    CTLocationAdapter getCtLocationAdapter() {
        return ctLocationAdapter;
    }

    CTGeofenceInterface getGeofenceInterface() {
        return ctGeofenceInterface;
    }

    public CTGeofenceSettings getGeofenceSettings() {
        return ctGeofenceSettings;
    }

    public static Logger getLogger() {
        return logger;
    }

    public interface OnGeofenceApiInitializedListener {
        void OnGeofenceApiInitialized();
    }
}
