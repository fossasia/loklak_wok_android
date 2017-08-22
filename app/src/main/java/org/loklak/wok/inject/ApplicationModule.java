package org.loklak.wok.inject;

import com.google.gson.Gson;

import org.loklak.wok.Utility;
import org.loklak.wok.api.loklak.LoklakAPI;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


@Module
public class ApplicationModule {

    private String mBaseUrl;

    public ApplicationModule(String baseUrl) {
        this.mBaseUrl = baseUrl;
    }

    @Provides
    Retrofit providesRetrofit() {
        Gson gson = Utility.getGsonForPrivateVariableClass();
        return new Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides
    LoklakAPI providesLoklakAPI(Retrofit retrofit) {
        return retrofit.create(LoklakAPI.class);
    }

    @Provides
    Realm providesRealm() {
        return Realm.getDefaultInstance();
    }
}
