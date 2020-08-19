package com.clevertap.android.geofence.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.clevertap.android.geofence.CTGeofenceAPI;
import com.clevertap.android.geofence.CTGeofenceConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

        CTGeofence.Builder setTransitionType(int transitionType) {
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

    @NonNull
    public static List<CTGeofence> from(@NonNull JSONArray jsonArray) {

        ArrayList<CTGeofence> geofenceList = new ArrayList<>();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject object = jsonArray.getJSONObject(i);
                CTGeofence geofence = new Builder(String.valueOf(object.getInt(CTGeofenceConstants.KEY_ID)))
                        .setLatitude(object.getDouble("lat"))
                        .setLongitude(object.getDouble("lng"))
                        .setRadius(object.getInt("r"))
                        .build();
                geofenceList.add(geofence);
            }
        } catch (JSONException e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Could not convert JSONArray to CTGeofence List");
            e.printStackTrace();
        }

        return geofenceList;

    }

    @Nullable
    public static CTGeofence from(@NonNull JSONObject object) {

        CTGeofence ctGeofence = null;

        try {
            ctGeofence = new Builder(String.valueOf(object.getInt(CTGeofenceConstants.KEY_ID)))
                    .setLatitude(object.getDouble("lat"))
                    .setLongitude(object.getDouble("lng"))
                    .setRadius(object.getInt("r"))
                    .build();

        } catch (JSONException e) {
            CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG, "Could not convert JSON to CTGeofence");
            e.printStackTrace();
        }

        return ctGeofence;

    }

    @NonNull
    public static List<String> toIdList(@NonNull Set<CTGeofence> set) {
        ArrayList<String> idList = new ArrayList<>();

        for (CTGeofence ctGeofence : set) {
            idList.add(ctGeofence.getId());
        }

        return idList;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CTGeofence)) return false;
        CTGeofence that = (CTGeofence) o;
        return transitionType == that.transitionType &&
                Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                radius == that.radius &&
                id.equals(that.id);
    }

    @Override
    public int hashCode() {
        int result = 17;

        result = 31 * result + (id == null ? 0 : id.hashCode());
        result = 31 * result + radius;
        result = 31 * result + transitionType;

        // Double.hashcode() implementation
        long lat = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (lat ^ (lat >>> 32));

        long lng = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (lng ^ (lng >>> 32));

        return result;
    }
}
