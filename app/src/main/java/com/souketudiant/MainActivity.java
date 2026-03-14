package com.souketudiant;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import io.realm.Realm;

import com.souketudiant.utils.DonneesTest;

public class MainActivity extends AppCompatActivity {

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation
        realm = Realm.getDefaultInstance();

        // Optionnel : générer des données de test si la base est vide
        DonneesTest.genererDonneesTest(realm);

        // TODO: Ici on chargera le fragment d'accueil plus tard
        // Pour l'instant, on laisse vide
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}