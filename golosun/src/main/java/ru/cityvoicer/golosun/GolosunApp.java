package ru.cityvoicer.golosun;

import android.app.Application;
import android.content.Context;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import ru.cityvoicer.golosun.model.AdList;
import ru.cityvoicer.golosun.model.Profile;

public class GolosunApp extends Application {
    private static Context mContext;
    private static GolosunApp mApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mApplication = this;

        //Utils.disableSSLCertificateChecking();

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, 128 << 20));

        Picasso built = builder.build();
        if (!Constants.isReleaseBuild()) {
            built.setIndicatorsEnabled(true);
            built.setLoggingEnabled(true);
        }
        Picasso.setSingletonInstance(built);

        Profile.getInstance();
        AdList.getInstance();
    }

    public static Context getContext() {
        return mContext;
    }

    public static GolosunApp getApplication() {
        return mApplication;
    }

    public static String getBaseFilePath() {
        return getContext().getFilesDir().getPath() + "/";
    }
}
