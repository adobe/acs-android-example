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

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DebugConsole extends Activity {
    private static final String TAG = DebugConsole.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_console);
        displayLogs();
    }


    @Override
    protected void onResume() {
        super.onResume();
        displayLogs();
        if (isNotificationChannelEnabled()) {
            Toast.makeText(this, "Notifications enabled for app", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Notifications enabled for " +  BuildConfig.APPLICATION_ID);
        } else {
            Toast.makeText(this, "Notifications disabled for app", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Notifications disabled for " + BuildConfig.APPLICATION_ID);
        }
    }

    public void copyClipboard(View view) {
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_LONG).show();
        TextView outputText = findViewById(R.id.debugConsole);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", outputText.getText());

        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
    }

    private void displayLogs() {
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log=new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line+"\n");
            }
            TextView outputText = findViewById(R.id.debugConsole);
            outputText.setMovementMethod(new ScrollingMovementMethod());
            outputText.setText(log.toString());

            //write to a file
            try {
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "2048-log-file.txt");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(log.toString().getBytes());
                fos.close();
            }
            catch (IOException e) {
                Log.e("Write Exception", "File write failed: " + e.toString());
            }

        } catch (IOException e) {
            // Handle Exception
        }
    }

    /**
     * help from here:
     * https://developer.android.com/reference/android/support/v4/app/NotificationManagerCompat#arenotificationsenabled
     * https://stackoverflow.com/questions/46928874/android-oreo-notifications-check-if-specific-channel-enabled/46998469#46998469
     */
    private boolean isNotificationChannelEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!TextUtils.isEmpty(AppConfig.EXAMPLE_CHANNEL_ID)) {
                NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

                if (manager != null) {
                    NotificationChannel channel = manager.getNotificationChannel(AppConfig.EXAMPLE_CHANNEL_ID);
                    if(channel != null) {
                        return (channel.getImportance() != NotificationManager.IMPORTANCE_NONE) && NotificationManagerCompat.from(this).areNotificationsEnabled();
                    }
                }
            }
            return false;
        } else {
            return NotificationManagerCompat.from(this).areNotificationsEnabled();
        }
    }
}
