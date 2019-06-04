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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.adobe.mobile.Analytics;
import com.adobe.mobile.Config;
import com.adobe.mobile.Visitor;
import com.neolane.android.v1.Neolane;
import com.neolane.android.v1.NeolaneAsyncRunner;
import com.neolane.android.v1.NeolaneException;

import java.io.IOException;
import java.util.HashMap;

public class CollectPIIActivity extends Activity {
    private final String TAG = CollectPIIActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.adobe.a2048.example.R.layout.activity_collect_pii);

        if (AppConfig.CAMPAIGN_STANDARD_ENABLED) {
            TextView mcidTextView = findViewById(com.adobe.a2048.example.R.id.mcidTextView);
            String mcid = Visitor.getMarketingCloudId();
            String mcidText = "MCID: " + mcid;
            mcidTextView.setText(mcidText);
        }

        TextView pushIdTextView = findViewById(com.adobe.a2048.example.R.id.pushIdTextView);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String pushId = preferences.getString(getString(com.adobe.a2048.example.R.string.push_token_key), null);
        String pushIdText = "Push ID: " + pushId;
        pushIdTextView.setText(pushIdText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        recordVisit();
    }

    public void collectPII(View view) {
        if (AppConfig.CAMPAIGN_STANDARD_ENABLED) {
            EditText firstNameEditText = findViewById(com.adobe.a2048.example.R.id.firstNameEditText);
            EditText lastNameEditText = findViewById(com.adobe.a2048.example.R.id.lastNameEditText);
            EditText emailEditText = findViewById(com.adobe.a2048.example.R.id.emailEditText);

            String firstNameText = firstNameEditText.getText().toString();
            String lastNameText = lastNameEditText.getText().toString();
            String emailText = emailEditText.getText().toString();
            String marketingCloudId = Visitor.getMarketingCloudId();

            HashMap<String, Object> data = new HashMap<>();
            data.put("triggerKey", "collectPII");
            data.put("marketingCloudId", marketingCloudId);
            data.put("firstName", firstNameText);
            data.put("lastName", lastNameText);
            data.put("email", emailText);
            Config.collectPII(data);

            Log.d(TAG, "MCID: " + marketingCloudId);
            Log.d(TAG, "First Name: " + firstNameText);
            Log.d(TAG, "Last Name: " + lastNameText);
            Log.d(TAG, "Email: " + emailText);

            Toast.makeText(this, "Collect PII Sent", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Collect PII disabled", Toast.LENGTH_LONG).show();
        }
    }

    public void recordVisit() {
        TextView pushMessageTextView = findViewById(com.adobe.a2048.example.R.id.pushMessageTextView);

        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        String pushMessageText = "Push body: ";

        if (data != null) {
            String message = data.getString("body");

            if (message != null) {
                pushMessageText += message;
            } else {
                pushMessageText += "body key not found";
            }
            pushMessageTextView.setText(pushMessageText);

            sendTracking(data);
        } else {
            pushMessageText += "not clicked";
        }

        pushMessageTextView.setText(pushMessageText);
    }

    private void sendTracking(Bundle data) {
        if (data == null) {
            return;
        }

        String deliveryId = data.getString("_dId");
        String messageId = data.getString("_mId");

        if (AppConfig.CAMPAIGN_STANDARD_ENABLED) {
            HashMap<String, Object> contextData = new HashMap<>();

            if (deliveryId != null && messageId != null) {
                contextData.put("deliveryId", deliveryId);
                contextData.put("broadlogId", messageId);
                contextData.put("action", "2");
                Analytics.trackAction("tracking", contextData);

                contextData.put("action", "7");
                Analytics.trackAction("tracking", contextData);
            }
        } else if (AppConfig.CAMPAIGN_CLASSIC_ENABLED) {
            // Neolane SDK
            // notify Neolane notification was opened
            if (deliveryId != null && messageId != null) {
                NeolaneAsyncRunner neolaneAs = new NeolaneAsyncRunner(Neolane.getInstance());
                neolaneAs.notifyOpening(Integer.valueOf(messageId), deliveryId, new NeolaneAsyncRunner.RequestListener() {
                    public void onNeolaneException(NeolaneException e, Object arg1) {
                        Log.e(TAG, "failure notifying push open to ACC", e);
                    }

                    public void onIOException(IOException e, Object arg1) {
                        Log.e(TAG, "failure notifying push open to ACC", e);
                    }

                    public void onComplete(String arg0, Object arg1) {
                        Log.d(TAG, "successfully notified push open to ACC");
                    }
                });
            }
        }
    }
}
