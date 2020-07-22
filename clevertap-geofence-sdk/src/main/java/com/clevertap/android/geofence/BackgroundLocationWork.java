package com.clevertap.android.geofence;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.clevertap.android.geofence.interfaces.CTLocationCallback;
import com.google.common.util.concurrent.ListenableFuture;

public class BackgroundLocationWork extends ListenableWorker {

    public BackgroundLocationWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {

        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                "Handling work in BackgroundLocationWork...");

        ListenableFuture<Result> listenableFuture = CallbackToFutureAdapter.getFuture(new CallbackToFutureAdapter.Resolver<Result>() {
            @Nullable
            @Override
            public Object attachCompleter(@NonNull final CallbackToFutureAdapter.Completer<Result> completer) throws Exception {

                final CTLocationCallback ctLocationCallback = new CTLocationCallback() {
                    @Override
                    public void onLocationComplete(Location location) {
                        CTGeofenceAPI.getInstance(getApplicationContext()).getGeofenceInterface()
                                .setLocationForGeofences(location,Utils.getGeofenceSDKVersion());

                        completer.set(Result.success());

                        CTGeofenceAPI.getLogger().debug(CTGeofenceAPI.GEOFENCE_LOG_TAG,
                                "BackgroundLocationWork is finished");
                    }
                };

                CTGeofenceTaskManager.getInstance().postAsyncSafely("ProcessLocationWork",
                        new Runnable() {
                            @Override
                            public void run() {

                                if (!Utils.initCTGeofenceApiIfRequired(getApplicationContext()))
                                {
                                    // if init fails then return without doing any work
                                    completer.set(Result.success());
                                    return;
                                }

                                CTGeofenceAPI.getInstance(getApplicationContext()).getCtLocationAdapter().getLastLocation(
                                        ctLocationCallback);

                            }
                        });


                return ctLocationCallback;
            }
        });

        return listenableFuture;

    }

}
