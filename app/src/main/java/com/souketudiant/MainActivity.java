package com.souketudiant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.souketudiant.fragments.AccueilFragment;
import com.souketudiant.fragments.ProfilFragment;
import com.souketudiant.fragments.PublierFragment;
import com.souketudiant.utils.DonneesTest;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation Realm
        realm = Realm.getDefaultInstance();

        // Générer des données de test si besoin
        DonneesTest.genererDonneesTest(realm);

        // Configuration de la navigation
        setupBottomNavigation();

        // Charger le fragment par défaut
        if (savedInstanceState == null) {
            chargerFragment(new AccueilFragment());
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.navigation_accueil) {
                fragment = new AccueilFragment();
            } else if (itemId == R.id.navigation_publier) {
                fragment = new PublierFragment();
            } else if (itemId == R.id.navigation_profil) {
                fragment = new ProfilFragment();
            }

            if (fragment != null) {
                chargerFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void chargerFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}