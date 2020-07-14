package com.clevertap.android.geofence;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;

import com.clevertap.android.geofence.interfaces.CTGeofenceAdapter;
import com.clevertap.android.geofence.interfaces.CTGeofenceCallback;
import com.clevertap.android.geofence.interfaces.CTGeofenceInterface;
import com.clevertap.android.geofence.interfaces.CTGeofenceTask;
import com.clevertap.android.geofence.interfaces.CTLocationCallback;
import com.clevertap.android.geofence.interfaces.CTLocationAdapter;

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
    private String accountId;
    private String guid;

    private CTGeofenceAPI(Context context) {
        this.context = context.getApplicationContext();
        ctLocationAdapter = CTLocationFactory.createLocationAdapter(this.context);
        ctGeofenceAdapter = CTGeofenceFactory.createGeofenceAdapter(this.context);
    }

    static {
        logger = new Logger(Logger.LogLevel.DEBUG);
    }

    @SuppressWarnings("WeakerAccess")
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
     * @param ctGeofenceSettings - Object of {@link CTGeofenceSettings}
     */
    @SuppressWarnings("unused")
    public void setGeofenceSettings(CTGeofenceSettings ctGeofenceSettings) {

        if (this.ctGeofenceSettings != null) {
            throw new IllegalStateException("Settings already configured.");
        }

        this.ctGeofenceSettings = ctGeofenceSettings;
    }

    /**
     * register an implementation of CTGeofenceInterface, should be used by CT SDK only
     *
     * @param ctGeofenceInterface - Object of {@link CTGeofenceInterface}
     */
    @SuppressWarnings("unused")
    public void setGeofenceInterface(CTGeofenceInterface ctGeofenceInterface) {
        this.ctGeofenceInterface = ctGeofenceInterface;
    }

    /**
     * Listener for Geofence SDK initialize
     *
     * @param onGeofenceApiInitializedListener - Object of {@link OnGeofenceApiInitializedListener}
     */
    @SuppressWarnings("unused")
    public void setOnGeofenceApiInitializedListener(OnGeofenceApiInitializedListener onGeofenceApiInitializedListener) {
        this.onGeofenceApiInitializedListener = onGeofenceApiInitializedListener;
    }

    /**
     * set clevertap account id, should be used by CT SDK only
     *
     * @param accountId clevertap account id
     */
    @SuppressWarnings("unused")
    public void setAccountId(String accountId) {

        if (accountId == null) {
            logger.debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Account Id is null");
            return;
        }

        this.accountId = accountId;
    }

    /**
     * set guid, should be used by CT SDK only
     *
     * @param guid - String
     */
    @SuppressWarnings("unused")
    public void setGUID(String guid) {

        if (guid == null) {
            logger.debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "guid is null");
            return;
        }

        this.guid = guid;
    }

    /**
     * Initializes location update receivers and geofence monitoring on background thread by
     * reading config settings if provided or will use default settings.
     * Should be used by CT SDK only
     */
    @SuppressWarnings("unused")
    public void activate() {

        if (isActivated) {
            logger.debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Geofence API already activated! dropping activate() call");
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

        isActivated = true;
        initBackgroundLocationUpdates();
    }

    @SuppressWarnings("WeakerAccess")
    public void initBackgroundLocationUpdates() {

        if (!Utils.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            logger.debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "We don't have ACCESS_FINE_LOCATION permission! Dropping initBackgroundLocationUpdates() call");
            return;
        }

        logger.debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "requestBackgroundLocationUpdates() called");

        if (!isActivated) {
            throw new IllegalStateException("Geofence SDK must be initialized before initBackgroundLocationUpdates()");
            //TODO: app is not calling activate() in any case, so this logging needs to change
        }

        LocationUpdateTask locationUpdateTask = new LocationUpdateTask(context);
        locationUpdateTask.setOnCompleteListener(new CTGeofenceTask.OnCompleteListener() {
            @Override
            public void onComplete() {

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
    @SuppressWarnings("unused")
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

                // delete cached files
                FileUtils.deleteDirectory(context,FileUtils.getCachedDirName(context));

                isActivated = false;
            }
        });


    }

    /**
     * fetches last location from FusedLocationAPI and delivers the result on main thread
     */
    @SuppressWarnings("unused")
    public void triggerLocation() {

        logger.debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "triggerLocation() called");

        if (!Utils.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            logger.debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "We don't have ACCESS_FINE_LOCATION permission! Dropping triggerLocation() call");
            return;
        }

        if (!isActivated) {
            throw new IllegalStateException("Geofence SDK must be initialized before triggerLocation()");
            //TODO: app is not calling activate() in any case, so this logging needs to change
        }

        CTGeofenceTaskManager.getInstance().postAsyncSafely("TriggerLocation",
                new Runnable() {
                    @Override
                    public void run() {
                        ctLocationAdapter.getLastLocation(new CTLocationCallback() {
                            @Override
                            public void onLocationComplete(Location location) {
                                //get's called on bg thread
                                ctGeofenceInterface.setLocationForGeofences(location);
                            }
                        });
                    }
                });


    }

    private CTGeofenceSettings initDefaultConfig() {
        return new CTGeofenceSettings.Builder().build();
    }

    @Override
    public void onSuccess(final JSONObject fenceList) {

        if (!Utils.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            logger.debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "We don't have ACCESS_FINE_LOCATION permission! dropping geofence update call");
            return;
        }

        if (!Utils.hasPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "We don't have ACCESS_BACKGROUND_LOCATION permission! dropping geofence update call");
            return;
        }

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

    @SuppressWarnings("WeakerAccess")
    public CTGeofenceSettings getGeofenceSettings() {
        return ctGeofenceSettings;
    }

    public static Logger getLogger() {
        return logger;
    }

    public interface OnGeofenceApiInitializedListener {
        void OnGeofenceApiInitialized();
    }

    String getAccountId() {
        return Utils.emptyIfNull(accountId);
    }

    String getGuid() {
        return Utils.emptyIfNull(guid);
    }
}
