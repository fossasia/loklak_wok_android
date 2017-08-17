package org.loklak.wok.api.twitter;


import org.loklak.wok.utility.Constants;

import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class TwitterMediaRestClient {

    private static TwitterOAuthInterceptor.Builder sMediaOAuthInterceptor =
            new TwitterOAuthInterceptor.Builder()
                    .consumerKey(Constants.KEY)
                    .consumerSecret(Constants.SECRET)
                    .random(new Random())
                    .clock(new TwitterOAuthInterceptor.Clock())
                    .onlyOauthParams(true);

    public static TwitterMediaAPI createTwitterMediaAPI(String accessToken, String accessSecret) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        TwitterOAuthInterceptor mediaInterceptor = sMediaOAuthInterceptor
                .accessToken(accessToken)
                .accessSecret(accessSecret).build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(mediaInterceptor)
                //.addInterceptor(loggingInterceptor) // uncomment to debug network requests
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TwitterMediaAPI.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        return retrofit.create(TwitterMediaAPI.class);
    }
}
