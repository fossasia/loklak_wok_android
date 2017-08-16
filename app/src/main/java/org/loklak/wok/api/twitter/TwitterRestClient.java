package org.loklak.wok.api.twitter;


import com.google.gson.Gson;

import org.loklak.wok.Utility;
import org.loklak.wok.utility.Constants;

import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class TwitterRestClient {

    // basic builders created
    private static TwitterOAuthInterceptor.Builder sInterceptorBuilder =
            new TwitterOAuthInterceptor.Builder()
                    .consumerKey(Constants.KEY)
                    .consumerSecret(Constants.SECRET)
                    .random(new Random())
                    .clock(new TwitterOAuthInterceptor.Clock());

    private static Gson mGson = Utility.getGsonForPrivateVariableClass();
    private static Retrofit.Builder sRetrofitBuilder = new Retrofit.Builder()
            .baseUrl(TwitterAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(mGson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

    // without access_token interceptor and client created
    private static TwitterOAuthInterceptor sWithoutAccessTokenInterceptor =
            sInterceptorBuilder.accessToken("").accessSecret("").build();

    // logger for debugging
    private static HttpLoggingInterceptor sLoggingInterceptor = new HttpLoggingInterceptor();

    private static OkHttpClient.Builder sWithoutAccessTokenClient = new OkHttpClient.Builder()
            .addInterceptor(sWithoutAccessTokenInterceptor);

    private static Retrofit sWithoutAccessTokenRetrofit;


    public static TwitterAPI createTwitterAPIWithoutAccessToken() {
        if (sWithoutAccessTokenRetrofit == null) {
            sLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            // uncomment to debug network requests
            // sWithoutAccessTokenClient.addInterceptor(sLoggingInterceptor);
            sWithoutAccessTokenRetrofit = sRetrofitBuilder
                    .client(sWithoutAccessTokenClient.build()).build();
        }
        return sWithoutAccessTokenRetrofit.create(TwitterAPI.class);
    }

    public static TwitterAPI createTwitterAPIWithAccessToken(String token) {
        TwitterOAuthInterceptor withAccessTokenInterceptor =
                sInterceptorBuilder.accessToken(token).accessSecret("").build();
        OkHttpClient withAccessTokenClient = new OkHttpClient.Builder()
                .addInterceptor(withAccessTokenInterceptor)
                //.addInterceptor(loggingInterceptor) // uncomment to debug network requests
                .build();
        Retrofit withAccessTokenRetrofit = sRetrofitBuilder.client(withAccessTokenClient).build();
        return withAccessTokenRetrofit.create(TwitterAPI.class);
    }

    public static TwitterAPI createTwitterAPIWithAccessTokenAndSecret(
            String token, String tokenSecret) {
        sLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        TwitterOAuthInterceptor withAccessTokenAndSecretInterceptor =
                sInterceptorBuilder.accessToken(token).accessSecret(tokenSecret).build();
        OkHttpClient withAccessTokenAndSecretClient = new OkHttpClient.Builder()
                .addInterceptor(withAccessTokenAndSecretInterceptor)
                 //.addInterceptor(loggingInterceptor) // uncomment to debug network requests
                .build();
        Retrofit withAccessTokenAndSecretRetrofit =
                sRetrofitBuilder.client(withAccessTokenAndSecretClient).build();
        return withAccessTokenAndSecretRetrofit.create(TwitterAPI.class);
    }
}
