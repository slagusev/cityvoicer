package ru.cityvoicer.golosun.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import ru.cityvoicer.golosun.Constants;
import ru.cityvoicer.golosun.model.Profile;


public class RegistrationIntentService extends IntentService {
    private static final String TAG = "RegistrationIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(Constants.GcmSenderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(TAG, "GCM Registration Token: " + token);

            token = "gcm:" + token;
            Profile.getInstance().setPushToken(token);
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }
    }
}
