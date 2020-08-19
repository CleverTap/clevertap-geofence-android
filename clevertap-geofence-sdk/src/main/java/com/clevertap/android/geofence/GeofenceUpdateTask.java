package com.clevertap.android.geofence;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.clevertap.android.geofence.interfaces.CTGeofenceAdapter;
import com.clevertap.android.geofence.interfaces.CTGeofenceTask;
import com.clevertap.android.geofence.model.CTGeofence;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adds/Replaces(remove followed by add) Geofences into file and Geofence Client
 */
class GeofenceUpdateTask implements CTGeofenceTask {

    private final Context context;
    @Nullable
    private final CTGeofenceAdapter ctGeofenceAdapter;
    @Nullable private final JSONObject fenceList;
    @Nullable
    private OnCompleteListener onCompleteListener;

    GeofenceUpdateTask(Context context, @Nullable JSONObject fenceList) {
        this.context = context.getApplicationContext();
        this.fenceList = fenceList;
        ctGeofenceAdapter = CTGeofenceAPI.getInstance(this.context).getCtGeofenceAdapter();
    }

    @WorkerThread
    @Override
    public void execute() {

        if (ctGeofenceAdapter == null)
            return;

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Executing GeofenceUpdateTask...");

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Reading previously registered geofences from file...");

        final JSONObject fenceSubList = getTrimmedGeofence(fenceList);

        String oldFenceListString = FileUtils.readFromFile(context,
                FileUtils.getCachedFullPath(context, CTGeofenceConstants.CACHED_FILE_NAME));

        if (!oldFenceListString.trim().equals("")) {

            JSONObject ctOldGeofenceObject = null;
            try {
                ctOldGeofenceObject = new JSONObject(oldFenceListString);
            } catch (Exception e) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Failed to read previously registered geofences from file");
                e.printStackTrace();
            }

            if (fenceList != null) {

                final Set<CTGeofence> newGeofenceSet = Utils.jsonToGeoFenceSet(fenceSubList); // A
                Set<CTGeofence> oldGeofenceSet = Utils.jsonToGeoFenceSet(ctOldGeofenceObject); // B
                final Set<CTGeofence> oldGeofenceSetCopy = Utils.jsonToGeoFenceSet(ctOldGeofenceObject); // B copy

                oldGeofenceSet.removeAll(newGeofenceSet);// set operation B - A
                newGeofenceSet.removeAll(oldGeofenceSetCopy); // set operation A - B

                //remove invalid geofences
                ctGeofenceAdapter.removeAllGeofence(CTGeofence.toIdList(oldGeofenceSet), new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        // called on same calling thread

                        // add brand new geofences
                        addGeofences(fenceSubList,new ArrayList<>(newGeofenceSet));
                    }
                });
            } else {
                // In case device reboot, boot receiver will pass null fenceList which simply means
                // read old fences from file and add back to Geofence Client
                try {
                    if (ctOldGeofenceObject!=null)
                    {
                        addGeofences(ctOldGeofenceObject,CTGeofence.from(ctOldGeofenceObject
                                .getJSONArray(CTGeofenceConstants.KEY_GEOFENCES)));

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            // add new fences
            try {
                if (fenceSubList != null) {
                    addGeofences(fenceSubList,CTGeofence.from(fenceSubList
                            .getJSONArray(CTGeofenceConstants.KEY_GEOFENCES)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Finished executing GeofenceUpdateTask");
    }

    /**
     * Extract top n geofences as requested by User through
     * {@link com.clevertap.android.geofence.CTGeofenceSettings.Builder#setGeofenceMonitoringCount(int)}
     * and store it to file followed by registration through
     * {@link GoogleGeofenceAdapter#addAllGeofence(List, OnSuccessListener)}
     *
     * @param fenceSubList json response containing list of geofences
     */
    @WorkerThread
    private void addGeofences(@Nullable final JSONObject fenceSubList, final List<CTGeofence> ctGeofenceList) {

        if (fenceSubList == null || ctGeofenceAdapter == null) {
            return;
        }


        //final List<CTGeofence> ctGeofenceList = CTGeofence.from(fenceSubList);

        ctGeofenceAdapter.addAllGeofence(ctGeofenceList, new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                // if geofences gets added successfully to OS then only write to file
                updateGeofencesToFile(ctGeofenceList.size(),fenceSubList);
                if (onCompleteListener != null) {
                    onCompleteListener.onComplete();
                }
            }
        });
    }

    private JSONObject getTrimmedGeofence(@Nullable JSONObject geofenceObject) {

        if (geofenceObject==null)
            return null;

        int geofenceMonitoringCount = CTGeofenceSettings.DEFAULT_GEO_MONITOR_COUNT;
        CTGeofenceSettings geofenceSettings = CTGeofenceAPI.getInstance(context).getGeofenceSettings();

        if (geofenceSettings != null) {
            geofenceMonitoringCount = geofenceSettings.getGeofenceMonitoringCount();
        }

        final JSONObject fenceSubList = new JSONObject();

        try {
            JSONArray geofenceObjectJSONArray = geofenceObject.getJSONArray(CTGeofenceConstants.KEY_GEOFENCES);

            if (geofenceMonitoringCount > geofenceObjectJSONArray.length()) {

                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Requested geofence monitoring count is greater than available count." +
                                " Setting request count to " + geofenceObjectJSONArray.length());

                geofenceMonitoringCount = geofenceObjectJSONArray.length();
            }

            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Extracting Top " + geofenceMonitoringCount + " new geofences out of " +
                            geofenceObjectJSONArray.length() + "...");

            JSONArray jsonSubArray = Utils.subArray(geofenceObjectJSONArray,
                    0, geofenceMonitoringCount);
            fenceSubList.put(CTGeofenceConstants.KEY_GEOFENCES, jsonSubArray);

            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Successfully created geofence sublist");
        } catch (Exception e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Failed to create geofence sublist");
            e.printStackTrace();
        }
        return fenceSubList;
    }

    @Override
    public void setOnCompleteListener(@NonNull OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    private void updateGeofencesToFile(int geofenceMonitoringCount,@NonNull JSONObject fenceSubList){

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Writing " + geofenceMonitoringCount + " new geofences to file...");

        //add new geofences, this will overwrite old ones
        boolean writeJsonToFile = FileUtils.writeJsonToFile(context, FileUtils.getCachedDirName(context),
                CTGeofenceConstants.CACHED_FILE_NAME, fenceSubList);

        if (writeJsonToFile) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "New geofences successfully written to file");
        } else {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Failed to write new geofences to file");
            if(CTGeofenceAPI.getInstance(context).getCleverTapApi() != null){
                CTGeofenceAPI.getInstance(context)
                        .getCleverTapApi()
                        .pushGeoFenceError(CTGeofenceConstants.ERROR_CODE,"Failed to write new geofences to file");
            }
        }
    }
}
