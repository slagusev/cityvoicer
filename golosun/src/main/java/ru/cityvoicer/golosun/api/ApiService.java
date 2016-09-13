package ru.cityvoicer.golosun.api;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.util.concurrent.CopyOnWriteArrayList;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import ru.cityvoicer.golosun.Constants;

public class ApiService {
    static private Api gApi;

    static public Api getApi() {
        if (gApi != null)
            return gApi;

        OkHttpClient client = new OkHttpClient();
        if (!Constants.isReleaseBuild()) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            client.interceptors().add(interceptor);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.ApiUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        gApi = retrofit.create(Api.class);
        return gApi;
    }

    public static int nextDelayTime(int delayMilis) {
        delayMilis *= 2;
        if (delayMilis < 250)
            delayMilis = 250;
        else if (delayMilis > 6000)
            delayMilis = 6000;
        delayMilis = delayMilis + (int)((double)delayMilis * Math.random() * 0.2);
        return delayMilis;
    }
}
