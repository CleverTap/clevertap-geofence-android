package com.clevertap.android.geofence;

public class CTGeofenceConstants {

    static final String EXTRA_JOB_SERVICE_TYPE = "job_service_type";
    static final int JOB_TYPE_DEVICE_BOOT = 1;

    static final String CACHED_DIR_NAME = "geofence";
    static final String CACHED_FILE_NAME = "geofence_cache.json";
    static final String SETTINGS_FILE_NAME = "geofence_settings.json";

    static final String ACTION_GEOFENCE_RECEIVER = "com.clevertap.android.geofence.fence.update";
    static final String ACTION_LOCATION_RECEIVER = "com.clevertap.android.geofence.location.update";

    static final String KEY_GEOFENCES = "geofences";
    public static final String KEY_ID = "id";
    static final String KEY_LAST_ACCURACY = "last_accuracy";
    static final String KEY_LAST_FETCH_MODE = "last_fetch_mode";
    static final String KEY_LAST_BG_LOCATION_UPDATES = "last_bg_location_updates";
    static final String KEY_LAST_LOG_LEVEL = "last_log_level";

    static final String TAG_WORK_LOCATION_UPDATES = "com.clevertap.android.geofence.work.location";

}
