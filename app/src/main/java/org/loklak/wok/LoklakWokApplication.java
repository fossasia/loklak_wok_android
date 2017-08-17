package org.loklak.wok;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class LoklakWokApplication extends Application {

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
    }

    @Override
    public void onTerminate() {
        Realm.getDefaultInstance().close();
        super.onTerminate();
    }
}
