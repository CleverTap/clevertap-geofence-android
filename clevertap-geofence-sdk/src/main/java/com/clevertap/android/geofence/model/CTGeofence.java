package com.clevertap.android.geofence.model;

import com.clevertap.android.geofence.CTGeofenceAPI;
import com.clevertap.android.geofence.CTGeofenceConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CTGeofence {


    private final int transitionType;
    private final String id;
    private final float latitude;
    private final float longitude;
    private final int radius;

    private CTGeofence(Builder builder) {
        id = builder.id;
        transitionType = builder.transitionType;
        latitude = builder.latitude;
        longitude = builder.longitude;
        radius = builder.radius;
    }

    public static final class Builder {

        private int transitionType;
        private String id;
        private float latitude;
        private float longitude;
        private int radius;

        public Builder(String id) {
            this.id = id;
        }

        public CTGeofence.Builder setTransitionType(int transitionType) {
            this.transitionType = transitionType;
            return this;
        }

        public CTGeofence.Builder setLatitude(float latitude) {
            this.latitude = latitude;
            return this;
        }

        public CTGeofence.Builder setLongitude(float longitude) {
            this.longitude = longitude;
            return this;
        }

        public CTGeofence.Builder setRadius(int radius) {
            this.radius = radius;
            return this;
        }

        public CTGeofence build() {
            CTGeofence ctGeofence = new CTGeofence(this);
            return ctGeofence;
        }
    }

    public String getId() {
        return id;
    }

    public int getTransitionType() {
        return transitionType;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public int getRadius() {
        return radius;
    }

    public static List<CTGeofence> from(JSONObject jsonObject) {

        ArrayList<CTGeofence> geofenceList = new ArrayList<>();

        try {
            JSONArray array = jsonObject.getJSONArray("geofences");

            if (array != null) {
                for (int i = 0; i < array.length(); i++) {

                    JSONObject object = array.getJSONObject(i);
                    CTGeofence geofence = new Builder(String.valueOf(object.getInt(CTGeofenceConstants.KEY_ID)))
                            .setLatitude((Float) object.get("lat"))
                            .setLongitude((Float) object.get("lng"))
                            .setRadius(object.getInt("r"))
                            .build();
                    geofenceList.add(geofence);
                }
            }
        } catch (JSONException e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Could not convert JSON to GeofenceList - " + e.getMessage());
            e.printStackTrace();
        }

        return geofenceList;

    }

    public static List<String> toIds(JSONObject jsonObject) {

        ArrayList<String> geofenceIdList = new ArrayList<>();

        try {
            JSONArray array = jsonObject.getJSONArray("geofences");

            if (array != null) {
                for (int i = 0; i < array.length(); i++) {

                    JSONObject object = array.getJSONObject(i);
                    geofenceIdList.add(object.getString("id"));
                }
            }
        } catch (JSONException e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                    "Could not convert JSON to GeofenceIdList - " + e.getMessage());
            e.printStackTrace();
        }

        return geofenceIdList;

    }
}
