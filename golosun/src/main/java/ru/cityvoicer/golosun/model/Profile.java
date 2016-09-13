package ru.cityvoicer.golosun.model;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.cityvoicer.golosun.GolosunApp;
import ru.cityvoicer.golosun.R;
import ru.cityvoicer.golosun.api.ApiService;
import ru.cityvoicer.golosun.api.NetProfileResponse;
import ru.cityvoicer.golosun.api.NetProfile;
import ru.cityvoicer.golosun.services.SoundBeepService;

public class Profile {
    private static String TAG = "Profile";

    public interface IProfileChangesCallback {
        void onChanged(Profile profile);
    }

    private transient boolean mActive;
    private transient Handler mHandler = new Handler();
    private transient boolean mSyncInProgress;
    private transient ArrayList<IProfileChangesCallback> mCallbacks = new ArrayList<IProfileChangesCallback>();

    private NetProfile mProfile;
    private int adVoteMoneyCounter;

    private transient Integer mPendingAge;
    private transient String mPendingSex;
    private transient String mPendingPushToken;
    private transient String mPendingAcMac;
    private transient boolean mNeedUpdateFlag;


    public void activate() {
        mActive = true;
        sync();
        tick();
    }

    public void deactivate() {
        mActive = false;
        mHandler.removeCallbacksAndMessages(null);
    }

    public boolean isRegistred() {
        return isInServerInitialRegistred() && mProfile.sex != null && !mProfile.sex.isEmpty() && mProfile.age != null && mProfile.age != 0;
    }

    public boolean isInServerInitialRegistred() {
        return mProfile != null && mProfile.username_mobile != null && mProfile.password_clear != null && mProfile.id != null;
    }

    public String getAutorization() {
        String credentials = mProfile.username_mobile + ":" + mProfile.password_clear;
        String basic = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        return basic;
    }

    public void sync() {
        sync(ApiService.nextDelayTime(0));
    }

    public void sync(final int delayAfterError) {
        if (mSyncInProgress || !mActive)
            return;

        if (mProfile == null || mProfile.username_mobile == null || mProfile.password_clear == null) {
            mSyncInProgress = true;
            ApiService.getApi().createUser(NetProfile.EXPAND_ALL_PARAMS).enqueue(new Callback<NetProfileResponse>() {
                @Override
                public void onResponse(Response<NetProfileResponse> response, Retrofit retrofit) {
                    mSyncInProgress = false;
                    if (response.isSuccess() && response.body() != null && response.body().success) {
                        mProfile = response.body().data;
                        save();
                        sync();

                        for (IProfileChangesCallback cb : mCallbacks) {
                            cb.onChanged(Profile.this);
                        }

                        AdList.getInstance().sync();
                    } else {
                        delayedSync(delayAfterError);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    mSyncInProgress = false;
                    delayedSync(delayAfterError);
                }
            });
            return;
        }

        if ((mPendingAge != null || mPendingSex != null || mPendingPushToken != null || mPendingAcMac != null) && mProfile != null && mProfile.username_mobile != null && mProfile.password_clear != null) {
            mSyncInProgress = true;
            final Integer reqPendingAge = mPendingAge;
            final String reqSex = mPendingSex;
            final String reqPushToken = mPendingPushToken;
            final String reqAcMac = mPendingAcMac;
            ApiService.getApi().updateProfile(getAutorization(), "balance_phiz,age,sex,push_token,ap_mac_address", mPendingAge, mPendingSex, mPendingPushToken, mPendingAcMac).enqueue(new Callback<NetProfileResponse>() {
                @Override
                public void onResponse(Response<NetProfileResponse> response, Retrofit retrofit) {
                    mSyncInProgress = false;

                    if (reqPendingAge != null && reqPendingAge.equals(mPendingAge)) {
                        mPendingAge = null;
                    }
                    if (reqSex != null && reqSex.equals(mPendingSex)) {
                        mPendingSex = null;
                    }
                    if (reqPushToken != null && reqPushToken.equals(mPendingPushToken)) {
                        mPendingPushToken = null;
                    }
                    if (reqAcMac != null && reqAcMac.equals(mPendingAcMac)) {
                        mPendingAcMac = null;
                    }

                    if (response.isSuccess() && response.body() != null && response.body().success) {
                        mNeedUpdateFlag = false;
                        update(response.body().data);
                        save();
                        sync();

                        AdList.getInstance().sync();
                        Cards.getInstance().sync();
                        Cards.getInstance().sync2();
                    } else {
                        delayedSync(delayAfterError);
                    }

                    for (IProfileChangesCallback cb : mCallbacks) {
                        cb.onChanged(Profile.this);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    mSyncInProgress = false;
                    if (!(t instanceof JsonSyntaxException)) {
                        delayedSync(delayAfterError);
                    }
                }
            });
            return;
        }

        if (mNeedUpdateFlag  && isRegistred()) {
            mSyncInProgress = true;
            ApiService.getApi().getProfile(getAutorization(), "balance_phiz,age,sex,push_token,ap_mac_address").enqueue(new Callback<NetProfileResponse>() {
                @Override
                public void onResponse(Response<NetProfileResponse> response, Retrofit retrofit) {
                    mSyncInProgress = false;

                    if (response.isSuccess() && response.body() != null && response.body().success) {
                        mNeedUpdateFlag = false;
                        update(response.body().data);
                        save();

                        for (IProfileChangesCallback cb : mCallbacks) {
                            cb.onChanged(Profile.this);
                        }
                    } else {
                        delayedSync(delayAfterError);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    mSyncInProgress = false;
                    delayedSync(delayAfterError);
                }
            });
            return;
        }
    }

    private void delayedSync(final int delayAfterError) {
        if (!mActive)
            return;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sync(ApiService.nextDelayTime(delayAfterError));
            }
        }, delayAfterError);
    }

    private void update(NetProfile profile) {
        /*
        if (profile.id != 0) {
            mProfile.id = profile.id;
        }

        if (profile.username_mobile != null) {
            mProfile.username_mobile = profile.username_mobile;
        }

        if (profile.password_clear != null) {
            mProfile.password_clear = profile.password_clear;
        }
        */

        if (profile.balance_phiz != null) {
            mProfile.balance_phiz = profile.balance_phiz;
            adVoteMoneyCounter = 0;
        }

        if (profile.age != null) {
            mProfile.age = profile.age;
        }

        if (profile.sex != null) {
            mProfile.sex = profile.sex;
        }

        if (profile.push_token != null) {
            mProfile.push_token = profile.push_token;
        }

        if (profile.ap_mac_address != null) {
            mProfile.ap_mac_address = profile.ap_mac_address;
        }
    }

    static private String filePath() {
        return GolosunApp.getBaseFilePath() + "profile.json";
    }

    private void save() {
        try {
            Writer writer = new FileWriter(filePath());
            Gson gson = new GsonBuilder().create();
            gson.toJson(this, writer);
            writer.close();
        } catch(Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    static private Profile gInstance;

    static public Profile getInstance() {
        if (gInstance != null)
            return gInstance;

        if ((new File(filePath())).exists()) {
            try {
                Reader reader = new FileReader(filePath());
                Gson gson = new GsonBuilder().create();
                gInstance = gson.fromJson(reader, Profile.class);
            } catch(Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }

        if (gInstance == null) {
            gInstance = new Profile();
        }


        /*
        gInstance.mProfile = new NetProfile();
        gInstance.mProfile.id = 233;
        gInstance.mProfile.username_mobile = "bf967";
        gInstance.mProfile.password_clear = 65956793;
        */


        return gInstance;
    }

    public void addListener(IProfileChangesCallback cb) {
        if (cb != null) {
            mCallbacks.add(cb);
        }
    }

    public void removeListener(IProfileChangesCallback cb) {
        mCallbacks.remove(cb);
    }

    public Integer getAgeSetPending() {
        return mPendingAge;
    }

    public String getSexSetPending() {
        return mPendingSex;
    }

    public void updateAgeAndSex(Integer age, String sex) {
        mPendingAge = age;
        mPendingSex = sex;
        sync();
    }

    public void sendMac() {
        String mac = null;
        try {
            WifiManager wifiManager = (WifiManager) GolosunApp.getContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            mac = wifiInfo.getBSSID();
        } catch (Exception ex) {
            return;
        }

        if (mac != null && !mac.isEmpty()) {
            if (mPendingAcMac == null && mProfile != null && mProfile.ap_mac_address != null && mProfile.ap_mac_address.equals(mac))
                return;
            mPendingAcMac = mac;
            sync();
        }
    }

    public void setPushToken(String token) {
        if (token != null && !token.isEmpty()) {
            if (mProfile != null && mProfile.push_token != null && mProfile.push_token.equals(token))
                return;
            mPendingPushToken = token;
            sync();
        }
    }

    public boolean isPushTokenExist() {
        return mProfile != null && mProfile.push_token != null && !mProfile.push_token.isEmpty();
    }

    private void tick() {
        mNeedUpdateFlag = true;
        sync();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tick();
            }
        }, 10000);
    }

    public String getBalance() {
        return mProfile.balance_phiz;
    }

    public Integer getUserId() {
        return mProfile.id;
    }

    public String getLogin() {
        return mProfile.username_mobile;
    }

    public int getPassword() {
        return mProfile.password_clear;
    }

    public int getAdVoteMoneyCounter() {
        return adVoteMoneyCounter;
    }

    public void incAdVoteMoneyCounter(int priority) {
        SoundBeepService.getInstance().beep(R.raw.message_sound);
        if (priority == 2) {
        } else {
            /*
            adVoteMoneyCounter ++;
            for (IProfileChangesCallback cb : mCallbacks) {
                cb.onChanged(Profile.this);
            }
            */
        }
    }
}
