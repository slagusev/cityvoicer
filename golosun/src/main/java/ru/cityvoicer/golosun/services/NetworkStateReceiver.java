package ru.cityvoicer.golosun.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import ru.cityvoicer.golosun.model.AdList;
import ru.cityvoicer.golosun.model.Profile;

public class NetworkStateReceiver extends BroadcastReceiver {
    private static Boolean isConnected = null;

    private static boolean getIsConnectedInternal(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE );
        NetworkInfo mobileNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isConnectedToMobile = mobileNetworkInfo != null && mobileNetworkInfo.isConnectedOrConnecting();

        NetworkInfo wifiNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isConnectedToWifi = wifiNetworkInfo != null && wifiNetworkInfo.isConnectedOrConnecting();

        return isConnectedToMobile || isConnectedToWifi;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isNowConnected = getIsConnectedInternal(context);
        if ((isConnected == null || !isConnected) && isNowConnected) {
            Profile.getInstance().sync();
            AdList.getInstance().preloadImages();
            AdList.getInstance().sync();
        }
        isConnected = new Boolean(isNowConnected);
    }

    public static boolean getIsConnected(Context context) {
        if (isConnected == null) {
            isConnected = new Boolean(getIsConnectedInternal(context));
        }

        return isConnected;
    }
}
