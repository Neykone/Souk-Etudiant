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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation Realm
        realm = Realm.getDefaultInstance();

        // Récupérer l'utilisateur connecté
        String utilisateurId = getIntent().getStringExtra("utilisateur_id");
        if (utilisateurId != null) {
            utilisateurConnecte = realm.where(Utilisateur.class)
                    .equalTo("id", utilisateurId)
                    .findFirst();
        }

        // Si pas d'utilisateur connecté, retourner à Login
        if (utilisateurConnecte == null) {
            retournerALogin();
            return;
        }

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
                // Passer l'utilisateur connecté au fragment
                Bundle bundle = new Bundle();
                bundle.putString("utilisateur_id", utilisateurConnecte.getId());
                fragment.setArguments(bundle);

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

    public void retournerALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Bien finir MainActivity
    }

    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}