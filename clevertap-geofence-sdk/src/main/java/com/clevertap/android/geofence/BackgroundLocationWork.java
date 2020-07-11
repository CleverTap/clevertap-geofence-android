package com.clevertap.android.geofence;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.clevertap.android.geofence.interfaces.CTLocatioCallback;
import com.google.common.util.concurrent.ListenableFuture;

class BackgroundLocationWork extends ListenableWorker {

    public BackgroundLocationWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {

        ListenableFuture<Result> listenableFuture = CallbackToFutureAdapter.getFuture(new CallbackToFutureAdapter.Resolver<Result>() {
            @Nullable
            @Override
            public Object attachCompleter(@NonNull final CallbackToFutureAdapter.Completer<Result> completer) throws Exception {

                CTLocatioCallback ctLocatioCallback = new CTLocatioCallback() {
                    @Override
                    public void onLocationComplete(Location location) {
                        CTGeofenceAPI.getInstance(getApplicationContext()).getGeofenceInterface()
                                .setLocationForGeofences(location);

                        completer.set(Result.success());
                    }
                };

                CTGeofenceAPI.getInstance(getApplicationContext()).getCtLocationAdapter().getLastLocation(
                        ctLocatioCallback);

                return ctLocatioCallback;
            }
        });

        return listenableFuture;

    }

}
