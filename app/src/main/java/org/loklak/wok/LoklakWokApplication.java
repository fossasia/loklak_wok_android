package org.loklak.wok;

import android.app.Application;

import org.loklak.wok.inject.ApplicationComponent;
import org.loklak.wok.inject.DaggerApplicationComponent;
import org.loklak.wok.inject.ApplicationModule;
import org.loklak.wok.utility.Constants;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class LoklakWokApplication extends Application {

    private ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        // TODO: uncomment the following and provide API key in manifest file to enable Crashlytics
        // Fabric.with(this, new Crashlytics());

        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(Constants.BASE_URL_LOKLAK))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }

    @Override
    public void onTerminate() {
        Realm.getDefaultInstance().close();
        super.onTerminate();
    }
}
