1. What is Play service APK error in logs?

   Current version of Geofence SDK requires device to have Play Services APK installed, enabled and up-to-date as describe [here](https://developers.google.com/android/guides/setup#ensure_devices_have_the_google_play_services_apk). In case if mentioned requirements not satisfied then SDK will silently catch the apk error and will printout error to logs and won't proceed further. You can add the same apk check on your end and then decide if you require to init `CTGeofenceAPI`. 

2. Does this sdk require to be compliant with any google policies ?

   Yes. Please check [location permissions section](https://support.google.com/googleplay/android-developer/answer/9888170?hl=en) and [background location access policy](https://support.google.com/googleplay/android-developer/answer/9799150?hl=en)