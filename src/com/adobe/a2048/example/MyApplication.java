/*
Copyright 2019 Adobe
All Rights Reserved.

NOTICE: Adobe permits you to use, modify, and distribute this file in
accordance with the terms of the Adobe license agreement accompanying
it. If you have received this file from a source other than Adobe,
then your use, modification, or distribution of it requires the prior
written permission of Adobe. 
*/

package com.adobe.a2048.example;

import android.app.Application;

import com.adobe.mobile.Config;

public class MyApplication extends Application {
    // Called when the application is starting, before any other application objects have been created.
    @Override
    public void onCreate() {
        super.onCreate();

        // allows Adobe Mobile SDK to
        if (AppConfig.CAMPAIGN_STANDARD_ENABLED) {
            /*
             Setting Adobe mobile v4 SDK context in Application class allows for entire app to
             allow the SDK access to the application context in all objects (currently provides
             context to MainActivity.java, MyFirebaseMessagingService.java, and CollectPIIActivity.java)
             */
            Config.setContext(this.getApplicationContext());

            //Turns on debug logging
            Config.setDebugLogging(AppConfig.CAMPAIGN_STANDARD_DEBUG_LOGGING_ENABLED);
        }
    }
}
