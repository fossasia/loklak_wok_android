package org.loklak.android.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class RestClient {

    private static final String BASE_URL = "https://api.loklak.org/";

    private static Gson gson = new GsonBuilder()
            .setFieldNamingStrategy(field -> {
                String name = FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(field);
                return name.substring(2); // because private fields are named as mName i.e m_name
            }).create();

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
