package com.souketudiant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.souketudiant.fragments.AccueilFragment;
import com.souketudiant.fragments.ProfilFragment;
import com.souketudiant.fragments.PublierFragment;
import com.souketudiant.models.Utilisateur;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private Realm realm;
    private Utilisateur utilisateurConnecte;
    private BottomNavigationView bottomNav;

    // Flag pour savoir si on est déjà sur l'Accueil
    private boolean estSurAccueil = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        realm = Realm.getDefaultInstance();

        utilisateurConnecte = realm.where(Utilisateur.class)
                .equalTo("estConnecte", true)
                .findFirst();

        if (utilisateurConnecte == null) {
            retournerALogin();
            return;
        }

        bottomNav = findViewById(R.id.bottom_navigation);

        setupBottomNavigation();

        // Charger l'Accueil au démarrage — SANS addToBackStack
        if (savedInstanceState == null) {
            chargerFragment(new AccueilFragment(), false);
            bottomNav.setSelectedItemId(R.id.navigation_accueil);
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.navigation_accueil) {
                fragment = new AccueilFragment();
                estSurAccueil = true;
            } else if (itemId == R.id.navigation_publier) {
                fragment = new PublierFragment();
                estSurAccueil = false;
            } else if (itemId == R.id.navigation_profil) {
                fragment = new ProfilFragment();
                estSurAccueil = false;
            }

            if (fragment != null) {
                // JAMAIS addToBackStack pour la BottomNav
                // sinon Back empile les fragments indéfiniment
                chargerFragment(fragment, false);
                return true;
            }
            return false;
        });
    }

    private void chargerFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Vider la back stack avant chaque navigation BottomNav
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        // Si on n'est PAS sur l'Accueil → revenir à l'Accueil
        if (!estSurAccueil) {
            estSurAccueil = true;
            bottomNav.setSelectedItemId(R.id.navigation_accueil);
            // setSelectedItemId déclenche le listener qui appelle chargerFragment
        } else {
            // On est déjà sur l'Accueil → quitter l'application
            super.onBackPressed();
        }
    }

    public void retournerALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}