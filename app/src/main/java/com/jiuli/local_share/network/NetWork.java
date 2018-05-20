package com.jiuli.local_share.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



public class NetWork {

    private Retrofit retrofit;

    private static class NetWorkI {
        private static NetWork instance = new NetWork();
        static {
            OkHttpClient client = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .connectTimeout(12, TimeUnit.SECONDS)
                    .build();

            instance.retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(Common.UPLOAD_API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
    }

    public static Retrofit getRetrofit() {
        return NetWorkI.instance.retrofit;
    }

}
