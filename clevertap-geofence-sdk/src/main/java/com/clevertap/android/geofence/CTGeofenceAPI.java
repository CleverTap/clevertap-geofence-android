package com.clevertap.android.geofence;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;

import com.clevertap.android.geofence.interfaces.CTGeofenceAdapter;
import com.clevertap.android.geofence.interfaces.CTGeofenceEventsListener;
import com.clevertap.android.geofence.interfaces.CTGeofenceTask;
import com.clevertap.android.geofence.interfaces.CTLocationCallback;
import com.clevertap.android.geofence.interfaces.CTLocationAdapter;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.GeofenceCallback;
import com.orhanobut.logger.DiskLogAdapter;

import org.json.JSONObject;

import static android.app.PendingIntent.FLAG_NO_CREATE;

public class CTGeofenceAPI implements GeofenceCallback {

    public static final String GEOFENCE_LOG_TAG = "CTGeofence";
    private static CTGeofenceAPI ctGeofenceAPI;
    private final Context context;
    private static Logger logger;
    private final CTLocationAdapter ctLocationAdapter;
    private final CTGeofenceAdapter ctGeofenceAdapter;

    private CTGeofenceSettings ctGeofenceSettings;
    private CleverTapAPI cleverTapAPI;
    private boolean isActivated;
    private OnGeofenceApiInitializedListener onGeofenceApiInitializedListener;
    private CTGeofenceEventsListener ctGeofenceEventsListener;
    private String accountId;
    private String guid;

    private CTGeofenceAPI(Context context) {
        this.context = context.getApplicationContext();
        com.orhanobut.logger.Logger.addLogAdapter(new DiskLogAdapter(this.context));
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


    public void init(CTGeofenceSettings ctGeofenceSettings, CleverTapAPI cleverTapAPI){
        setCleverTapApi(cleverTapAPI);
        setGeofenceSettings(ctGeofenceSettings);
        setAccountId(cleverTapAPI.getAccountId());
        activate();
    }

    /**
     * Set geofence API configuration settings
     *
     * @param ctGeofenceSettings - Object of {@link CTGeofenceSettings}
     */
    @SuppressWarnings("unused")
    void setGeofenceSettings(CTGeofenceSettings ctGeofenceSettings) {

        if (this.ctGeofenceSettings != null) {
            logger.debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Settings already configured");
            return;
        }

        this.ctGeofenceSettings = ctGeofenceSettings;
    }


    @SuppressWarnings("unused")
    void setCleverTapApi(CleverTapAPI cleverTapAPI) {
        this.cleverTapAPI = cleverTapAPI;
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
     * set clevertap account id
     *
     * @param accountId clevertap account id
     */
    @SuppressWarnings("unused")
    void setAccountId(String accountId) {

        if (accountId == null || accountId.isEmpty()) {
            logger.debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Account Id is null or empty");
            return;
        }

        this.accountId = accountId;
    }


    /**
     * Initializes location update receivers and geofence monitoring on background thread by
     * reading config settings if provided or will use default settings.
     */
    @SuppressWarnings("unused")
    void activate() {

        if (isActivated) {
            logger.debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Geofence API already activated! dropping activate() call");
            return;
        }

        if (cleverTapAPI == null) {
            throw new IllegalStateException("CleverTap instance is null");
        }

        if (ctGeofenceSettings == null) {
            ctGeofenceSettings = initDefaultConfig();
        }

        logger.setDebugLevel(ctGeofenceSettings.getLogLevel());


        cleverTapAPI.setGeofenceCallback(this);
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

    @Override
    public void handleGeoFences(JSONObject fenceList) {
        if (!Utils.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            logger.debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "We don't have ACCESS_FINE_LOCATION permission! dropping geofence update call");
            return;
        }

        if (!Utils.hasBackgroundLocationPermission(context)) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "We don't have ACCESS_BACKGROUND_LOCATION permission! dropping geofence update call");
            return;
        }

        if (fenceList == null) {
            logger.debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Geofence response is null! dropping further processing");
            return;
        }

/*        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put(CTGeofenceConstants.KEY_ID,"8001");
            jsonObject.put("lat","20.061722727398493");
            jsonObject.put("lng","21.061722727398493");
            jsonObject.put("r","200");
            jsonObject.put("gcId","1");
            jsonObject.put("gcName","Test1");

            fenceList.getJSONArray("geofences").put(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/


        GeofenceUpdateTask geofenceUpdateTask = new GeofenceUpdateTask(context, fenceList);

        CTGeofenceTaskManager.getInstance().postAsyncSafely("ProcessGeofenceUpdates",
                geofenceUpdateTask);
    }

    /**
     * fetches last location from FusedLocationAPI
     */
    @SuppressWarnings("unused")
    @Override
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
                                cleverTapAPI.setLocationForGeofences(location,Utils.getGeofenceSDKVersion());
                            }
                        });
                    }
                });


    }

    private CTGeofenceSettings initDefaultConfig() {
        return new CTGeofenceSettings.Builder().build();
    }

    public CTGeofenceEventsListener getCtGeofenceEventsListener() {
        return ctGeofenceEventsListener;
    }

    public void setCtGeofenceEventsListener(CTGeofenceEventsListener ctGeofenceEventsListener) {
        this.ctGeofenceEventsListener = ctGeofenceEventsListener;
    }

    CTGeofenceAdapter getCtGeofenceAdapter() {
        return ctGeofenceAdapter;
    }

    CTLocationAdapter getCtLocationAdapter() {
        return ctLocationAdapter;
    }

    CleverTapAPI getCleverTapApi() {
        return cleverTapAPI;
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
}
