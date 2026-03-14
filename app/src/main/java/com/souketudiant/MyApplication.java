package com.souketudiant;

import android.app.Application;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialiser Realm
        Realm.init(this);

        // Configuration de Realm
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("souketudiant.realm")
                .schemaVersion(1)
                .allowWritesOnUiThread(true)
                .deleteRealmIfMigrationNeeded()
                .build();

        // Définir la configuration par défaut
        Realm.setDefaultConfiguration(config);
    }
}
