package com.clevertap.android.geofenceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.clevertap.android.geofence.CTGeofenceAPI;
import com.clevertap.android.geofence.CTGeofenceSettings;
import com.clevertap.android.geofence.Logger;
import com.clevertap.android.geofence.interfaces.CTGeofenceEventsListener;
import com.clevertap.android.geofence.interfaces.CTLocationUpdatesListener;
import com.clevertap.android.sdk.CleverTapAPI;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private Button mBtnInit;
    private Button mBtnTriggerLocation;
    private Button mBtnDeactivate;
    private CleverTapAPI mCleverTapInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnInit = findViewById(R.id.btnInit);
        mBtnTriggerLocation = findViewById(R.id.btnTriggerLocation);
        mBtnDeactivate = findViewById(R.id.btnDeactivate);

        mCleverTapInstance = CleverTapAPI.getDefaultInstance(this);
        CleverTapAPI.setDebugLevel(10);

        mBtnInit.setOnClickListener(this);
        mBtnTriggerLocation.setOnClickListener(this);
        mBtnDeactivate.setOnClickListener(this);

    }

    private void initCTGeofenceApi() {
        if (mCleverTapInstance == null)
            return;

        // proceed only if cleverTap instance is not null
        final Context con = this;
        CTGeofenceAPI.getInstance(getApplicationContext())
                .init(new CTGeofenceSettings.Builder()
                        .enableBackgroundLocationUpdates(true)
                        .setLogLevel(Logger.DEBUG)
                        .setLocationAccuracy(CTGeofenceSettings.ACCURACY_HIGH)
                        .setLocationFetchMode(CTGeofenceSettings.FETCH_CURRENT_LOCATION_PERIODIC)
                        .setGeofenceMonitoringCount(99)
                        .setInterval(3600000) // 1 hour
                        .setFastestInterval(1800000) // 30 minutes
                        .setSmallestDisplacement(1000)// 1 km
                        .build(), mCleverTapInstance);

        CTGeofenceAPI.getInstance(getApplicationContext())
                .setOnGeofenceApiInitializedListener(new CTGeofenceAPI.OnGeofenceApiInitializedListener() {
                    @Override
                    public void OnGeofenceApiInitialized() {
                        Toast.makeText(con, "Geofence API initialized", Toast.LENGTH_SHORT).show();
                    }
                });

        CTGeofenceAPI.getInstance(getApplicationContext())
                .setCtGeofenceEventsListener(new CTGeofenceEventsListener() {
                    @Override
                    public void onGeofenceEnteredEvent(JSONObject jsonObject) {
                        Toast.makeText(con, "Geofence Entered", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onGeofenceExitedEvent(JSONObject jsonObject) {
                        Toast.makeText(con, "Geofence Exited", Toast.LENGTH_SHORT).show();
                    }
                });

        CTGeofenceAPI.getInstance(getApplicationContext())
                .setCtLocationUpdatesListener(new CTLocationUpdatesListener() {
                    @Override
                    public void onLocationUpdates(Location location) {
                        Toast.makeText(con, "Location updated", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int fineLocationPermissionState = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);

        int backgroundLocationPermissionState = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) : PackageManager.PERMISSION_GRANTED;

        return (fineLocationPermissionState == PackageManager.PERMISSION_GRANTED) &&
                (backgroundLocationPermissionState == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissions() {
        boolean permissionAccessFineLocationApproved =
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        boolean backgroundLocationPermissionApproved =
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        boolean shouldProvideRationale =
                permissionAccessFineLocationApproved && backgroundLocationPermissionApproved;

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                initCTGeofenceApi();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnInit:
                if (mCleverTapInstance != null) {
                    // proceed only if cleverTap instance is not null
                    if (!checkPermissions()) {
                        requestPermissions();
                    } else {
                        initCTGeofenceApi();
                    }
                }
                break;
            case R.id.btnTriggerLocation:
                try {
                    CTGeofenceAPI.getInstance(getApplicationContext()).triggerLocation();
                } catch (IllegalStateException e) {
                    // geofence not initialized
                    e.printStackTrace();
                    // init geofence
                    initCTGeofenceApi();
                }
                break;
            case R.id.btnDeactivate:
                CTGeofenceAPI.getInstance(getApplicationContext()).deactivate();
                break;
        }
    }
}
