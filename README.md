ACS Android Example with Adobe Mobile SDK v4
============

Code based on https://github.com/uberspot/2048-android, which is the android port of the 2048 game made by Gabriele Cirulli https://github.com/gabrielecirulli/2048.

## :warning::warning: IMPORTANT :warning::warning:
* App is missing google-services.json, so make sure to get it before using this project. https://firebase.google.com/docs/android/setup

## Building

    cd acs-android-example/
    git submodule update --init --recursive
    ./gradlew build

### Running app With Android Studio

1. Follow first three lines of Building directions.
2. In Android Studio 3.4.1 select File > Open... and select acs-android-example directory.
3. Run > Run 'acs-android-example'

## License

acs-android-example is licensed under the [MIT license](https://github.com/adobe/acs-android-example/blob/master/LICENSE).

### How to add Adobe Mobile SDK v4 to app
* https://marketing.adobe.com/resources/help/en_US/mobile/android/dev_qs.html

### Add push messaging with Adobe Mobile SDK v4
* https://marketing.adobe.com/resources/help/en_US/mobile/android/push_messaging.html
* NOTE: for firebase don't need permissions in AndroidManifest.xml (https://developers.google.com/cloud-messaging/android/android-migrate-fcm):
    * <your-package-name>.permission.C2D_MESSAGE
    * android.permission.WAKE_LOCK 

### How to Integrate the Adobe Mobile v4 SDK with a mobile app to receive Adobe Campaign Standard push notifications 
* https://helpx.adobe.com/campaign/kb/integrate-mobile-sdk.html
* https://helpx.adobe.com/campaign/standard/administration/using/configuring-mobile-app-channel.html

### How to copy existing project with new name
* https://stackoverflow.com/questions/18324555/android-copy-existing-project-with-new-name-in-android-studio/30377608#30377608
    * also need to get new google-services.json from https://console.firebase.google.com/ by adding an app to existing 2048 project
