package ru.cityvoicer.golosun.model;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import me.leolin.shortcutbadger.ShortcutBadger;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.cityvoicer.golosun.GolosunActivity;
import ru.cityvoicer.golosun.GolosunApp;
import ru.cityvoicer.golosun.Constants;
import ru.cityvoicer.golosun.api.ApiService;
import ru.cityvoicer.golosun.api.NetAdItem;
import ru.cityvoicer.golosun.api.NetAdListResponse;
import ru.cityvoicer.golosun.api.NetResponseBase;
import ru.cityvoicer.golosun.services.NetworkStateReceiver;

public class AdList {
    private static String TAG = "AdList";

    private transient Handler mHandler = new Handler();
    private transient boolean mInProgress;
    private transient boolean mActive;
    private transient boolean mNeedUpdateAds;

    private ArrayList<NetAdItem> mAdItems = new ArrayList<NetAdItem>();
    private ArrayList<Vote> mVotesPending = new ArrayList<Vote>();

    public NetAdItem findBestAdToShow() {
        if (mAdItems.isEmpty())
            return null;
        int priority = mAdItems.get(0).priority;
        for (NetAdItem item : mAdItems) {
            if (item.priority != priority)
                break;
            String url = Constants.fixImageUrl(item.image);
            if (url == null || mPreloadedImages.containsKey(url)) {
                return item;
            }
        }
        return mAdItems.get(0);
    }

    public NetAdItem findBestSecondAdToShow() {
        if (mAdItems.size() <= 1)
            return null;
        return mAdItems.get(1);
    }

    public boolean isEmpty() {
        return mAdItems.isEmpty();
    }

    public void vote(NetAdItem item, EVote value, String text) {
        if (!mAdItems.contains(item))
            return;
        NetAdItem ad = mAdItems.remove(mAdItems.indexOf(item));
        Vote vote = new Vote();
        vote.adId = ad.id;
        vote.vote = value;
        vote.text = text;
        vote.id_places = ad.id_places;
        mVotesPending.add(vote);

        Profile.getInstance().incAdVoteMoneyCounter(ad.priority);

        setBadgerCount();

        sync(0);
    }

    public void activate() {
        mActive = true;
        mNeedUpdateAds = true;
        mHandler.removeCallbacksAndMessages(null);
        sync();
        if (mRePreload || mPreloadList.size() > 0) {
            preloadImages();
        }
    }

    public void deactivate() {
        mActive = false;
        mHandler.removeCallbacksAndMessages(null);
    }

    public void sync() {
        sync(ApiService.nextDelayTime(0));
    }

    private void sync(final int delayAfterError) {
        if (!mActive || !Profile.getInstance().isRegistred() || mInProgress)
            return;

        if (!mVotesPending.isEmpty()) {
            mInProgress = true;
            Vote vote = mVotesPending.get(0);
            ApiService.getApi().adVote(Profile.getInstance().getAutorization(), vote.adId, vote.text, vote.vote.get(), vote.id_places).enqueue(new Callback<NetResponseBase>() {
                @Override
                public void onResponse(Response<NetResponseBase> response, Retrofit retrofit) {
                    mInProgress = false;
                    if (response.isSuccess()) {
                        mVotesPending.remove(0);
                        save();
                        sync();
                    } else {
                        delayedSync(delayAfterError);
                    }
                }
                @Override
                public void onFailure(Throwable t) {
                    mInProgress = false;
                    delayedSync(delayAfterError);
                }
            });
            return;
        }

        if (mAdItems.size() <= Constants.TargetAdsCount / 2 || mNeedUpdateAds) {
            mInProgress = true;
            mNeedUpdateAds = false;
            ApiService.getApi().getAdList(Profile.getInstance().getAutorization(), Constants.TargetAdsCount, 1).enqueue(new Callback<NetAdListResponse>() {
                @Override
                public void onResponse(Response<NetAdListResponse> response, Retrofit retrofit) {
                    mInProgress = false;
                    if (response.isSuccess() && response.body().data != null) {
                        ArrayList<NetAdItem> ads = response.body().data.items;
                        boolean updated = false;
                        if (ads != null) {
                            for (NetAdItem ad : ads) {
                                boolean finded = false;
                                for (NetAdItem a : mAdItems) {
                                    if (a.id.equals(ad.id)) {
                                        finded = true;
                                        updated |= a.update(ad);
                                        break;
                                    }
                                }
                                for (Vote vote : mVotesPending) {
                                    if (vote.adId.equals(ad.id)) {
                                        finded = true;
                                        break;
                                    }
                                }
                                if (!finded) {
                                    updated = true;
                                    mAdItems.add(ad);
                                }
                            }
                        }

                        if (updated) {
                            Collections.sort(mAdItems, new Comparator<NetAdItem>() {
                                @Override
                                public int compare(NetAdItem a, NetAdItem b) {
                                    if (a.priority < b.priority)
                                        return -1;
                                    if (a.priority > b.priority)
                                        return 1;
                                    if (a.date < b.date)
                                        return -1;
                                    if (a.date > b.date)
                                        return 1;
                                    return 0;
                                }
                            });

                            save();

                            preloadImages();
                        }

                        setBadgerCount();

                        if (GolosunActivity.gActivity != null) {
                            GolosunActivity.gActivity.lockSideMenuHack();
                        }

                        if (!updated && mVotesPending.isEmpty()) {
                            delayedSync(10000);
                        } else {
                            sync();
                        }
                    } else {
                        delayedSync(delayAfterError);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    mInProgress = false;
                    delayedSync(delayAfterError);
                }
            });
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

    static private String filePath() {
        return GolosunApp.getBaseFilePath() + "adlist.json";
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

    static private AdList gInstance;

    static public AdList getInstance() {
        if (gInstance != null)
            return gInstance;

        if ((new File(filePath())).exists()) {
            try {
                Reader reader = new FileReader(filePath());
                Gson gson = new GsonBuilder().create();
                gInstance = gson.fromJson(reader, AdList.class);
            } catch(Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }

        if (gInstance == null) {
            gInstance = new AdList();
        }

        gInstance.setBadgerCount();

        gInstance.preloadImages();

        return gInstance;
    }

    void setBadgerCount() {
        ShortcutBadger.with(GolosunApp.getApplication().getApplicationContext()).count(gInstance.mAdItems.size());
    }

    enum ChainLoaderWhatImage {
        Logo,
        Image
    }

    private transient boolean mRePreload;
    private transient int mPreloadSequenceNumber;
    private transient ArrayList<String> mPreloadList = new ArrayList<String>();
    private transient ChainImageLoaderCallback mImageLoaderCallback;
    private transient HashMap<String, Boolean> mPreloadedImages = new HashMap<String, Boolean>();

    public void preloadImages() {
        mPreloadSequenceNumber ++;
        mPreloadList.clear();

        if (!mActive) {
            mRePreload = true;
            return;
        }

        mRePreload = false;

        for (NetAdItem item : mAdItems) {
            if (item.isVideo())
                continue;
            String logo = Constants.fixImageUrl(item.logo);
            if (logo != null && !mPreloadList.contains(logo)) {
                mPreloadList.add(logo);
            }

            String image = Constants.fixImageUrl(item.image);
            if (image != null && !mPreloadList.contains(image)) {
                mPreloadList.add(image);
            }
        }

        preloadItem();
    }

    void preloadItem() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mPreloadList.isEmpty())
                    return;

                mImageLoaderCallback = new ChainImageLoaderCallback(mPreloadSequenceNumber);
            }
        });
    }

    private class ChainImageLoaderCallback implements Target {
        private int it_number;

        public ChainImageLoaderCallback(int it_number) {
            this.it_number = it_number;
            Picasso.with(GolosunApp.getContext()).load(mPreloadList.get(0)).into(this);
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if (it_number != mPreloadSequenceNumber)
                return;

            mPreloadedImages.put(mPreloadList.get(0), true);
            mPreloadList.remove(0);
            preloadItem();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            if (it_number != mPreloadSequenceNumber)
                return;

            if (NetworkStateReceiver.getIsConnected(GolosunApp.getContext())) {
                mPreloadedImages.put(mPreloadList.get(0), false);
                preloadItem();
            }
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    }
}
