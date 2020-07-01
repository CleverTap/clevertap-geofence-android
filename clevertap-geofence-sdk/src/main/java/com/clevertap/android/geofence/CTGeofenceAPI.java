package com.clevertap.android.geofence;

import android.content.Context;

import com.clevertap.android.geofence.interfaces.CTGeofenceCallback;
import com.clevertap.android.geofence.interfaces.CTGeofenceContract;
import com.clevertap.android.geofence.model.CTGeofence;
import com.clevertap.android.geofence.model.CTGeofenceSettings;

import org.json.JSONObject;

import java.util.List;

public class CTGeofenceAPI implements CTGeofenceCallback {

    public static final String GEOFENCE_LOG_TAG = "CTGeofence";
    private static CTGeofenceAPI ctGeofenceAPI;
    private final Context context;
    private static Logger logger;
    private final CTGeofenceManager ctGeofenceManager;
    private CTGeofenceSettings ctGeofenceSettings;
    private CTGeofenceContract ctGeofenceContract;

    private CTGeofenceAPI(Context context) {
        this.context = context;
        logger = new Logger(Logger.LogLevel.DEBUG);
        ctGeofenceManager = new CTGeofenceManager(context);

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
     * Set geofence API configuration settings
     *
     * @param ctGeofenceSettings
     */
    public void setGeofenceAPISettings(CTGeofenceSettings ctGeofenceSettings) {
        this.ctGeofenceSettings = ctGeofenceSettings;
    }

    /**
     * register an implementation of CTGeofenceContract
     *
     * @param ctGeofenceContract
     */
    public void setGeofenceContract(CTGeofenceContract ctGeofenceContract) {
        this.ctGeofenceContract = ctGeofenceContract;
    }

    public CTGeofenceContract getGeofenceContract() {
        return ctGeofenceContract;
    }

    public CTGeofenceSettings getGeofenceSettings() {
        return ctGeofenceSettings;
    }

    public static Logger getLogger() {
        return logger;
    }

    public void activate() {

        if (ctGeofenceContract == null) {
            throw new IllegalStateException("setGeofenceContract() must be called before activate");
        }

        if (ctGeofenceSettings == null) {
            ctGeofenceSettings = initDefaultConfig();
        }

        logger.setDebugLevel(ctGeofenceSettings.getLogLevel());

        /**
         * TODO: activate fence and location receivers and basic initialization
         */

        ctGeofenceContract.setGeoFenceCallback(this);
        logger.debug(GEOFENCE_LOG_TAG, "geofence callback registered");


    }

    private CTGeofenceSettings initDefaultConfig() {
        return new CTGeofenceSettings.Builder().build();
    }

    public void deactivate() {

        /**
         * TODO: deactivate fence and location receivers and clean up resources
         */

    }

    @Override
    public void onSuccess(JSONObject fenceList) {

        if (fenceList != null) {
            FileUtils.writeJsonToFile(context, this, CTGeofenceConstants.CACHED_DIR_NAME,
                    CTGeofenceConstants.CACHED_FILE_NAME, fenceList);
            List<CTGeofence> ctGeofenceList = CTGeofence.from(fenceList);

            if (!ctGeofenceList.isEmpty())
            {
                ctGeofenceManager.addAllGeofence(ctGeofenceList);
            }

        }
    }

    @Override
    public void onFailure(Throwable error) {

    }

}
