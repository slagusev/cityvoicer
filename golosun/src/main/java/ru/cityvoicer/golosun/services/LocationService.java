package ru.cityvoicer.golosun.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import ru.cityvoicer.golosun.GolosunActivity;
import ru.cityvoicer.golosun.GolosunApp;


public class LocationService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = LocationManager.class.getSimpleName();

    private static final int INTERVAL = 3000;
    private static final float DISPLACEMENT = 50.0f;

    private static LocationService instance;
    private Location mLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    public static LocationService getInstance() {
        if (instance == null) {
            instance = new LocationService();
        }
        return instance;
    }

    private LocationService() {
        mGoogleApiClient = new GoogleApiClient.Builder(GolosunApp.getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(INTERVAL / 2);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mGoogleApiClient.connect();
    }

    public Location getLocation() {
        return mLocation;
    }

    private boolean started = false;

    public boolean isStarted() {
        return started;
    }

    public void start() {
        if (started || !mGoogleApiClient.isConnected())
            return;

        Location loc = null;
        try  {
            loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException ex) {
            Log.i(TAG, ex.toString());
        }

        if (loc != null) {
            onLocationChanged(loc);
        }

        try  {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            started = true;
        } catch (SecurityException ex) {
            Log.i(TAG, ex.toString());
        }
    }

    public void stop() {
        if (!started)
            return;

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        started = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle arg0) {
        if (GolosunActivity.gActiveActivity != null) {
            GolosunActivity.gActiveActivity.startLocationService();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
    }
}
