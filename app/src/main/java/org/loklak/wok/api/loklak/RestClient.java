package org.loklak.wok.api.loklak;

import com.google.gson.Gson;

import org.loklak.wok.Utility;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class RestClient {

    private static final String BASE_URL = "https://api.loklak.org/";

    private static Gson gson = Utility.getGsonForPrivateVariableClass();
    private static Retrofit sRetrofit;

    private RestClient() {
    }

    private static void createRestClient() {
        sRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    private static Retrofit getRetrofitInstance() {
        if (sRetrofit == null) {
            createRestClient();
        }
        return sRetrofit;
    }

    public static <T> T createApi(Class<T> apiInterface) {
        return getRetrofitInstance().create(apiInterface);
    }

}
