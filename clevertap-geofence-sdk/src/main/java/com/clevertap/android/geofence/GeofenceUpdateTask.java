package com.clevertap.android.geofence;

import android.content.Context;

import com.clevertap.android.geofence.interfaces.CTGeofenceAdapter;
import com.clevertap.android.geofence.interfaces.CTGeofenceTask;
import com.clevertap.android.geofence.model.CTGeofence;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.util.List;

/**
 * Adds/Replaces(remove followed by add) Geofences into file and Geofence Client
 */
class GeofenceUpdateTask implements CTGeofenceTask {

    private final Context context;
    private final CTGeofenceAdapter ctGeofenceAdapter;
    private final JSONObject fenceList;
    private OnCompleteListener onCompleteListener;

    GeofenceUpdateTask(Context context, JSONObject fenceList) {
        this.context = context.getApplicationContext();
        this.fenceList = fenceList;
        ctGeofenceAdapter = CTGeofenceAPI.getInstance(this.context).getCtGeofenceAdapter();
    }

    @Override
    public void execute() {

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Executing GeofenceUpdateTask...");

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Reading previously registered geofences from file...");

        String oldFenceListString = FileUtils.readFromFile(context,
                FileUtils.getCachedFullPath(context,CTGeofenceConstants.CACHED_FILE_NAME));

        if (oldFenceListString != null && !oldFenceListString.trim().equals("")) {

            List<String> ctOldGeofenceIdList = null;
            JSONObject ctOldGeofenceObject = null;
            try {
                ctOldGeofenceObject = new JSONObject(oldFenceListString);
                ctOldGeofenceIdList = Utils.jsonToGeoFenceList(ctOldGeofenceObject);
            } catch (Exception e) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Failed to read previously registered geofences from file");
                e.printStackTrace();
            }

            if (fenceList != null) {
                //remove previously added geofences
                ctGeofenceAdapter.removeAllGeofence(ctOldGeofenceIdList, new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        // called on same calling thread
                        addGeofences(fenceList);
                    }
                });
            } else {
                // In case device reboot, boot receiver will pass null fenceList which simply means
                // read old fences from file and add back to Geofence Client
                addGeofences(ctOldGeofenceObject);
            }
        } else {
            // add new fences
            addGeofences(fenceList);
        }

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Finished executing GeofenceUpdateTask");
    }

    private void addGeofences(JSONObject fenceList) {

        if (fenceList == null) {
            return;
        }
        //TODO add debug logging here

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Writing new geofences to file...");

        //add new geofences, this will overwrite old ones
        boolean writeJsonToFile = FileUtils.writeJsonToFile(context, FileUtils.getCachedDirName(context),
                CTGeofenceConstants.CACHED_FILE_NAME, fenceList);

        if (writeJsonToFile)
        {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "New geofences successfully written to file");
        } else {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Failed to write new geofences to file");
        }

        List<CTGeofence> ctGeofenceList = CTGeofence.from(fenceList);

        ctGeofenceAdapter.addAllGeofence(ctGeofenceList, new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                if (onCompleteListener != null) {
                    onCompleteListener.onComplete();
                }
            }
        });
    }

    @Override
    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }
}
