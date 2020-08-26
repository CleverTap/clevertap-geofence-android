<p align="center">
  <img src="https://github.com/CleverTap/clevertap-segment-ios/blob/master/clevertap-logo.png" width="230"/>
</p>

## clevertap-geofence-android

## Table of contents

* [Introduction](#introduction)
* [Installation](#installation)
* [Manifest Permissions](#manifest-permissions)
* [Runtime Location Permission](#runtime-location-permission)
* [Initialization](#initialization)
* [Settings parameters](#settings-parameters)
* [Trigger Location](#trigger-location)
* [Callbacks/Listeners](#callbackslisteners)
* [Deactivation](#deactivation)
* [Example Usage](#example-usage)
* [FAQ](#faq)

## Introduction
[(Back to top)](#table-of-contents)

CleverTap Geofence SDK provides Geofencing capabilities to CleverTap Android SDK by using the Play Services Location library. If you haven't already configured your project for Play Services, follow the instructions [here](https://developers.google.com/android/guides/google-services-plugin).

## Installation
[(Back to top)](#table-of-contents)

Add the following dependencies to the ```build.gradle```

```
implementation 'com.clevertap.android:geofence:1.0.0'
implementation 'com.clevertap.android:clevertap-android-sdk:3.9.0' // 3.9.0 and above
implementation 'com.google.android.gms:play-services-location:17.0.0'
implementation 'androidx.work:work-runtime:2.3.4' // required for FETCH_LAST_LOCATION_PERIODIC
implementation 'androidx.concurrent:concurrent-futures:1.0.0' // required for FETCH_LAST_LOCATION_PERIODIC
```
## Manifest Permissions
[(Back to top)](#table-of-contents)

In order to start using geofence in your app, the app will need below permissions in `AndroidManifest.xml` which is already added by SDK so you don’t have to add anything in manifest.
```
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
```

## Runtime Location Permission
[(Back to top)](#table-of-contents)

```
android.permission.ACCESS_FINE_LOCATION
android.permission.ACCESS_BACKGROUND_LOCATION (Required only when requesting background location access on Android 10 (API level 29))
```

Before SDK initialization App will need to prompt users to grant above permissions at runtime. Since users can revoke permissions at any time from the app Settings screen, your app needs to check that it has the permissions it needs every time it runs.
See [Permissions](https://developer.android.com/preview/features/runtime-permissions.html) and [Location Permissions](https://developer.android.com/training/location/permissions#request-location-access-runtime) for more details.

## Initialization
[(Back to top)](#table-of-contents)

`CTGeofenceAPI` needs an object of `CTGeofenceSettings` and the object of `CleverTapAPI` to be initialized in the following manner-

```CTGeofenceAPI.getInstance(getApplicationContext()).init(ctGeofenceSettings,cleverTapAPI);```

`CTGeofenceSettings` object can be created in the following way -
```
CTGeofenceSettings ctGeofenceSettings = new CTGeofenceSettings.Builder()
                .enableBackgroundLocationUpdates(bgLocation)//boolean to enable background location updates
                .setLogLevel(logLevel)//Log Level
                .setLocationAccuracy(accuracy)//byte value for Location Accuracy
                .setLocationFetchMode(fetchMode)//byte value for Fetch Mode
                .setGeofenceMonitoringCount(geofenceCount)//int value for number of Geofences CleverTap can monitor
                .setInterval(interval)//long value for interval
                .setFastestInterval(fastestInterval)//long value for fastest interval
                .setSmallestDisplacement(displacement)//float value for smallest Displacement
                .build();
 ```
**Note** - 

* CleverTapAPI needs to be initialized before CTGeofenceAPI object is created as the object of CleverTapAPI is required to initialize the Geofence SDK.
* Runtime location permissions needed for SDK to work
* CTGeofenceAPI is automatically activated once the init() method has been called.
* CT Geofence SDK will raise the “Geofence Cluster Entered” and “Geofence Cluster Exited” events automatically. The app cannot raise these methods manually.

## Settings parameters
[(Back to top)](#table-of-contents)

Detailed info can be found [here](https://github.com/CleverTap/clevertap-geofence-android/wiki/Geofence-Settings)

## Trigger Location
[(Back to top)](#table-of-contents)

This method fetches last known location from OS (can be null) and delivers it to APP through `CTLocationUpdatesListener`. 
Detailed info can be found [here](https://github.com/CleverTap/clevertap-geofence-android/wiki/Trigger-Location)

```
try {
    CTGeofenceAPI.getInstance(getApplicationContext()).triggerLocation();
}catch (IllegalStateException e)
{
    // thrown when this method is called before geofence sdk initiaisation
}
```

## Callbacks/Listeners
[(Back to top)](#table-of-contents)

CleverTap Geofence SDK provides 3 callbacks or listeners on the main thread to the app for more control to the developers.

### OnGeofenceApiInitializedListener

CT Geofence SDK provides a callback on the main thread to notify that the CTGeofenceAPI class has been initialized. The callback can be used in the following way - 
```
CTGeofenceAPI.getInstance(getApplicationContext())
       .setOnGeofenceApiInitializedListener(new CTGeofenceAPI.OnGeofenceApiInitializedListener() {
           @Override
           public void OnGeofenceApiInitialized() {
               //App is notified on the main thread that CTGeofenceAPI is initialized
           }
       });
 ```

### CTGeofenceEventsListener

CT Geofence SDK provides a callback on the main thread to the app when the user enters or exits a Geofence. The callback can be used in the following way -
```
CTGeofenceAPI.getInstance(getApplicationContext())
       .setCtGeofenceEventsListener(new CTGeofenceEventsListener() {
           @Override
           public void onGeofenceEnteredEvent(JSONObject jsonObject) {
                //Callback on the main thread when user enters Geofence with info in jsonObject             
           }

           @Override
           public void onGeofenceExitedEvent(JSONObject jsonObject) {
               //Callback on the main thread when user exits Geofence with info in jsonObject             
           }
       });
```

Structure of JSON Object on Geofence Entered/Exited event :

```
{
      "id" : 500043 //geofenceUniqueID,
      "gcId" : 1 //geofenceClusterId,
      "gcName" : "geofenceClusterName",
      "lat" : 19.229493674459807 //geofence Latitude,
      "lng" : 72.82329440116882 //geofence Longitude,
      "r" : 200 //geofenceRadiusInMeters,
      "triggered_lat" : 19.229493674459807 //geofenceTriggeredLatitude,
      "triggered_lng" : 72.82329440116882 //geofenceTriggeredLongitude
   }
 ```
 
### CTLocationUpdatesListener

CT Geofence SDK provides a callback on the main thread to the app when Android OS provides a location update to the CT Geofence SDK. The callback can be used in the following way -
```
CTGeofenceAPI.getInstance(getApplicationContext())
       .setCtLocationUpdatesListener(new CTLocationUpdatesListener() {
           @Override
           public void onLocationUpdates(Location location) {
              //New location on the main thread as provided by the Android OS
           }
       });
 ```

## Deactivation
[(Back to top)](#table-of-contents)

If at any point you want to deactivate the CT Geofence SDK, you can do so in the following way-

```CTGeofenceAPI.getInstance(getApplicationContext()).deactivate();```

**Note:** Deactivation will remove all callbacks/listeners as well. 

## Example Usage
[(Back to top)](#table-of-contents)

A [demo application](https://github.com/CleverTap/clevertap-geofence-android/tree/feature/geofence/SDK-264/app) showing geofence sdk integration.

## FAQ
[(Back to top)](#table-of-contents)

FAQ can be found [here](https://github.com/CleverTap/clevertap-geofence-android/wiki/FAQ). If your question is not found in FAQ and you have other questions or concerns, you can reach out to the CleverTap support team at [support@clevertap.com](mailto:support@clevertap.com).
