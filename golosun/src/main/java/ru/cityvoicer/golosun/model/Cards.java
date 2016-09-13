package ru.cityvoicer.golosun.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import me.leolin.shortcutbadger.ShortcutBadger;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.cityvoicer.golosun.Constants;
import ru.cityvoicer.golosun.GolosunApp;
import ru.cityvoicer.golosun.api.ApiService;
import ru.cityvoicer.golosun.api.NetAdItem;
import ru.cityvoicer.golosun.api.NetAdListResponse;
import ru.cityvoicer.golosun.api.NetGetCardResponse;
import ru.cityvoicer.golosun.api.NetResponseBase;
import ru.cityvoicer.golosun.services.LocationService;
import ru.cityvoicer.golosun.services.NetworkStateReceiver;

public class Cards {
    private static String TAG = "Cards";

    public interface ICardsChangesCallback {
        void onChanged(Cards cards);
    }

    private transient Handler mHandler = new Handler();
    private transient boolean mActive = false;
    private transient boolean mInProgress = false;
    private transient boolean mInProgress2 = false;

    private String mBarCodeImageUrl;
    private String mBarCodeImageFilePath;
    private String mBarCodeNumber;
    private transient Bitmap mBarCodeImage;
    private transient Target mBarCodeImageLoader;

    private Location nCardUseLocation;

    public void notifyBarCodeShowed() {
        Location loc = LocationService.getInstance().getLocation();
        if (loc != null) {
            nCardUseLocation = loc;
        } else {
            nCardUseLocation = new Location("empty");
        }
        save();
        sync2();
    }

    @Nullable
    public Bitmap getBarCodeImage() {
        if (mBarCodeImage != null) {
            return mBarCodeImage;
        }
        return null;
    }

    @Nullable
    public String getBarCodeNumber() {
        return mBarCodeNumber;
    }

    public void activate() {
        mActive = true;
        mHandler.removeCallbacksAndMessages(null);
        sync();
        sync2();
    }

    public void deactivate() {
        mActive = false;
        mHandler.removeCallbacksAndMessages(null);
    }

    public void sync() {
        sync(ApiService.nextDelayTime(0));
    }

    public void sync2() {
        sync2(ApiService.nextDelayTime(0));
    }

    private void sync2(final int delayAfterError) {
        if (!mActive || !Profile.getInstance().isRegistred() || mInProgress2)
            return;

        if (nCardUseLocation != null) {
            mInProgress2 = true;
            ApiService.getApi().sendCardUsedNotification("D3)6OW]$K[UU1QL0RCOA", "" + Profile.getInstance().getUserId(), "location", nCardUseLocation.getLatitude(), nCardUseLocation.getLongitude()).enqueue(new Callback<NetResponseBase>() {
                @Override
                public void onResponse(Response<NetResponseBase> response, Retrofit retrofit) {
                    mInProgress2 = false;
                    if (response.isSuccess()) {
                        nCardUseLocation = null;
                        save();
                        sync2();
                    } else {
                        delayedSync2(delayAfterError);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    mInProgress2 = false;
                    delayedSync2(delayAfterError);
                }
            });
            return;
        }
    }

    private void sync(final int delayAfterError) {
        if (!mActive || !Profile.getInstance().isRegistred() || mInProgress)
            return;

        if (mBarCodeImageUrl == null) {
            mInProgress = true;
            ApiService.getApi().getCard("D3)6OW]$K[UU1QL0RCOA", "" + Profile.getInstance().getUserId(),  "getcart").enqueue(new Callback<NetGetCardResponse>() {
                @Override
                public void onResponse(Response<NetGetCardResponse> response, Retrofit retrofit) {
                    mInProgress = false;
                    if (response.isSuccess()) {
                        mBarCodeImageUrl = response.body().data.barcode;
                        mBarCodeNumber = response.body().data.num_barcode;
                        mBarCodeImageFilePath = null;
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

        if (mBarCodeImageUrl != null && mBarCodeImageFilePath == null && mBarCodeImageLoader == null) {
            mInProgress = true;

            mBarCodeImageLoader = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mBarCodeImageLoader = null;
                    mInProgress = false;
                    mBarCodeImageFilePath = GolosunApp.getBaseFilePath() + "barcode.png";
                    try {
                        OutputStream fOut = null;
                        File file = new File(mBarCodeImageFilePath);
                        fOut = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                        fOut.close();
                        save();
                    } catch (Exception ex) {
                        mBarCodeImageFilePath = null;
                    }
                    sync();
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    mBarCodeImageLoader = null;
                    mInProgress = false;
                    delayedSync(delayAfterError);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };

            Picasso.with(GolosunApp.getContext()).load(mBarCodeImageUrl).into(mBarCodeImageLoader);
            return;
        }

        if (mBarCodeImage == null && mBarCodeImageFilePath != null) {
            mBarCodeImage = BitmapFactory.decodeFile(mBarCodeImageFilePath);
            sync();
            for (ICardsChangesCallback cb : mCallbacks) {
                cb.onChanged(this);
            }
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

    private void delayedSync2(final int delayAfterError) {
        if (!mActive)
            return;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sync2(ApiService.nextDelayTime(delayAfterError));
            }
        }, delayAfterError);
    }

    static private String filePath() {
        return GolosunApp.getBaseFilePath() + "cards.json";
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

    static private Cards gInstance;

    static public Cards getInstance() {
        if (gInstance != null)
            return gInstance;

        if ((new File(filePath())).exists()) {
            try {
                Reader reader = new FileReader(filePath());
                Gson gson = new GsonBuilder().create();
                gInstance = gson.fromJson(reader, Cards.class);
            } catch(Exception ex) {
                Log.e(TAG, ex.toString());
            }
        }

        if (gInstance == null) {
            gInstance = new Cards();
        }

        return gInstance;
    }

    transient private ArrayList<ICardsChangesCallback> mCallbacks = new ArrayList<>();

    public void addListener(ICardsChangesCallback cb) {
        if (cb != null) {
            mCallbacks.add(cb);
        }
    }

    public void removeListener(ICardsChangesCallback cb) {
        mCallbacks.remove(cb);
    }
}

