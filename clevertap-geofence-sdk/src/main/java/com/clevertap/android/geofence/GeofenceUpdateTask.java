package com.clevertap.android.geofence;

import android.content.Context;

import com.clevertap.android.geofence.interfaces.CTGeofenceAdapter;
import com.clevertap.android.geofence.interfaces.CTGeofenceTask;
import com.clevertap.android.geofence.model.CTGeofence;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.util.List;

/**
 *  Adds/Replaces(remove followed by add) Geofences into file and Geofence Client
 */
class GeofenceUpdateTask implements CTGeofenceTask {

    private final Context context;
    private final CTGeofenceAdapter ctGeofenceAdapter;
    private final JSONObject fenceList;
    private OnCompleteListener onCompleteListener;

    GeofenceUpdateTask(Context context, JSONObject fenceList) {
        this.context = context;
        this.fenceList = fenceList;
        ctGeofenceAdapter = CTGeofenceAPI.getInstance(context).getCtGeofenceAdapter();
    }

    @Override
    public void execute() {

        String oldFenceListString = FileUtils.readFromFile(context, CTGeofenceConstants.CACHED_FULL_PATH);

        if (oldFenceListString != null && !oldFenceListString.trim().equals("")) {
            //remove previously added geofences
            List<String> ctOldGeofenceList = null;
            try {
                ctOldGeofenceList = CTGeofence.toIds(new JSONObject(oldFenceListString));
            } catch (Exception e) {
                CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                        "Failed to read previously registered geofences from file");
                e.printStackTrace();
            }

            ctGeofenceAdapter.removeAllGeofence(ctOldGeofenceList, new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    // called on same calling thread
                    addGeofences();
                }
            });
        } else {
            // add new fences
            addGeofences();
        }
    }

    private void addGeofences() {
        //add new geofences, this will overwrite old ones
        FileUtils.writeJsonToFile(context, CTGeofenceConstants.CACHED_DIR_NAME,
                CTGeofenceConstants.CACHED_FILE_NAME, fenceList);
        List<CTGeofence> ctGeofenceList = CTGeofence.from(fenceList);

        ctGeofenceAdapter.addAllGeofence(ctGeofenceList, new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                if (onCompleteListener!=null)
                {
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
