package ru.cityvoicer.golosun.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import me.leolin.shortcutbadger.ShortcutBadger;
import ru.cityvoicer.golosun.GolosunActivity;
import ru.cityvoicer.golosun.GolosunApp;
import ru.cityvoicer.golosun.R;

public class MyGcmListenerService extends GcmListenerService {
    private static final String TAG = "MyGcmListenerService";

    public static final String BADGE = "badge";
    public static final String MESSAGE = "message";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "From: " + from);
        String extras = "";
        for (String key : data.keySet()) {
            extras += "\n" + key + ": " + data.get(key);
        }
        Log.d(TAG, "Data: " + extras);

        if (GolosunActivity.gActiveActivity != null)
            return;

        if (data.containsKey(BADGE)) {
            try {
                String str = data.get(BADGE).toString();
                int count = Integer.parseInt(str);
                ShortcutBadger.with(GolosunApp.getApplication().getApplicationContext()).count(count);
            } catch (Exception ex) {
                Log.d(TAG, ex.toString());
            }
        }

        if (data.containsKey(MESSAGE)) {
            String message = data.get(MESSAGE).toString();
            NotificationService.sendNotification(getApplicationContext(), message, data);
        }
    }
}
