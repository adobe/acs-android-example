/*
Copyright 2019 Adobe
All Rights Reserved.

NOTICE: Adobe permits you to use, modify, and distribute this file in
accordance with the terms of the Adobe license agreement accompanying
it. If you have received this file from a source other than Adobe,
then your use, modification, or distribution of it requires the prior
written permission of Adobe.
*/

package com.adobe.a2048.example.service;

import android.support.annotation.NonNull;
import android.util.Log;

import com.adobe.a2048.example.BuildConfig;

import androidx.work.Worker;

/**
 * help from here:
 * https://developer.android.com/topic/libraries/architecture/adding-components#add_the_google_maven_repository
 * https://developer.android.com/topic/libraries/architecture/adding-components#workmanager
 * https://medium.com/mindorks/lets-work-manager-do-background-processing-58356e1ab844
 */
public class KeepAppAliveWorker extends Worker {
    private final String TAG = KeepAppAliveWorker.class.getSimpleName();

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork(): " + BuildConfig.APPLICATION_ID);
        return Result.SUCCESS;
    }
}
