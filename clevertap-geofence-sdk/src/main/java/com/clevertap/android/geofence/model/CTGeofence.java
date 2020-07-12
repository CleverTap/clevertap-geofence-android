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
    private final double latitude;
    private final double longitude;
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
        private double latitude;
        private double longitude;
        private int radius;

        Builder(String id) {
            this.id = id;
        }

        public CTGeofence.Builder setTransitionType(int transitionType) {
            this.transitionType = transitionType;
            return this;
        }

        CTGeofence.Builder setLatitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        CTGeofence.Builder setLongitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        CTGeofence.Builder setRadius(int radius) {
            this.radius = radius;
            return this;
        }

        CTGeofence build() {
            return new CTGeofence(this);
        }
    }

    public String getId() {
        return id;
    }

    public int getTransitionType() {
        return transitionType;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getRadius() {
        return radius;
    }

    public static List<CTGeofence> from(JSONObject jsonObject) {

        ArrayList<CTGeofence> geofenceList = new ArrayList<>();

        try {
            JSONArray array = jsonObject.getJSONArray("geofences");

            for (int i = 0; i < array.length(); i++) {

                JSONObject object = array.getJSONObject(i);
                CTGeofence geofence = new Builder(String.valueOf(object.getInt(CTGeofenceConstants.KEY_ID)))
                        .setLatitude(object.getDouble("lat"))
                        .setLongitude(object.getDouble("lng"))
                        .setRadius(object.getInt("r"))
                        .build();
                geofenceList.add(geofence);
            }
        } catch (JSONException e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Could not convert JSON to GeofenceList - " + e.getMessage());
            e.printStackTrace();
        }

        return geofenceList;

    }
}
