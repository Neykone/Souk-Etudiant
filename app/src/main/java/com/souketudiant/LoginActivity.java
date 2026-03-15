package com.souketudiant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.souketudiant.models.Utilisateur;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class LoginActivity extends AppCompatActivity {

    private Realm realm;
    private TabLayout tabLayout;
    private View loginView;
    private View registerView;

    // Champs de connexion
    private EditText editTextLoginEmail;
    private EditText editTextLoginPassword;
    private Button buttonLogin;

    // Champs d'inscription
    private EditText editTextRegisterNom;
    private EditText editTextRegisterEmail;
    private EditText editTextRegisterPassword;
    private EditText editTextRegisterConfirmPassword;
    private EditText editTextRegisterTelephone;
    private EditText editTextRegisterFiliere;
    private Button buttonRegister;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialisation de Realm
        realm = Realm.getDefaultInstance();

        // Vérifier si un utilisateur est déjà connecté
        Utilisateur utilisateurConnecte = realm.where(Utilisateur.class)
                .equalTo("estConnecte", true)
                .findFirst();
        if (utilisateurConnecte != null) {
            // Rediriger directement vers MainActivity
            startMainActivity(utilisateurConnecte);
            return;
        }

        initViews();
        setupTabLayout();
        setupClickListeners();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        loginView = findViewById(R.id.loginView);
        registerView = findViewById(R.id.registerView);
        progressBar = findViewById(R.id.progressBar);

        // Vues de connexion
        editTextLoginEmail = findViewById(R.id.editTextLoginEmail);
        editTextLoginPassword = findViewById(R.id.editTextLoginPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        // Vues d'inscription
        editTextRegisterNom = findViewById(R.id.editTextRegisterNom);
        editTextRegisterEmail = findViewById(R.id.editTextRegisterEmail);
        editTextRegisterPassword = findViewById(R.id.editTextRegisterPassword);
        editTextRegisterConfirmPassword = findViewById(R.id.editTextRegisterConfirmPassword);
        editTextRegisterTelephone = findViewById(R.id.editTextRegisterTelephone);
        editTextRegisterFiliere = findViewById(R.id.editTextRegisterFiliere);
        buttonRegister = findViewById(R.id.buttonRegister);
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("Connexion"));
        tabLayout.addTab(tabLayout.newTab().setText("Inscription"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Onglet Connexion
                    loginView.setVisibility(View.VISIBLE);
                    registerView.setVisibility(View.GONE);
                } else {
                    // Onglet Inscription
                    loginView.setVisibility(View.GONE);
                    registerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupClickListeners() {
        buttonLogin.setOnClickListener(v -> loginUser());
        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private void loginUser() {
        String email = editTextLoginEmail.getText().toString().trim();
        String password = editTextLoginPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(email)) {
            editTextLoginEmail.setError("Email requis");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextLoginPassword.setError("Mot de passe requis");
            return;
        }

        showProgress(true);

        // Rechercher l'utilisateur dans Realm (synchronously d'abord pour vérifier)
        Utilisateur utilisateur = realm.where(Utilisateur.class)
                .equalTo("email", email)
                .equalTo("motDePasse", password)
                .findFirst();

        if (utilisateur != null) {
            // Utilisateur trouvé, procéder à la connexion
            String userId = utilisateur.getId();

            // Mettre à jour le statut de connexion
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    // Déconnecter tous les autres utilisateurs
                    RealmResults<Utilisateur> utilisateursConnectes = bgRealm.where(Utilisateur.class)
                            .equalTo("estConnecte", true)
                            .findAll();
                    for (Utilisateur u : utilisateursConnectes) {
                        u.setEstConnecte(false);
                    }

                    // Connecter l'utilisateur courant
                    Utilisateur user = bgRealm.where(Utilisateur.class)
                            .equalTo("id", userId)
                            .findFirst();
                    if (user != null) {
                        user.setEstConnecte(true);
                    }
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    showProgress(false);
                    Toast.makeText(LoginActivity.this, "Connexion réussie !", Toast.LENGTH_SHORT).show();

                    // Récupérer l'utilisateur à jour
                    Utilisateur userConnected = realm.where(Utilisateur.class)
                            .equalTo("id", userId)
                            .findFirst();
                    startMainActivity(userConnected);
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    showProgress(false);
                    error.printStackTrace();
                    Toast.makeText(LoginActivity.this,
                            "Erreur lors de la connexion: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();

                    // Essayer quand même de démarrer l'activité
                    startMainActivity(utilisateur);
                }
            });
        } else {
            showProgress(false);
            Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_LONG).show();
        }
    }

    private void registerUser() {
        String nom = editTextRegisterNom.getText().toString().trim();
        String email = editTextRegisterEmail.getText().toString().trim();
        String password = editTextRegisterPassword.getText().toString().trim();
        String confirmPassword = editTextRegisterConfirmPassword.getText().toString().trim();
        String telephone = editTextRegisterTelephone.getText().toString().trim();
        String filiere = editTextRegisterFiliere.getText().toString().trim();

        // Validations
        if (TextUtils.isEmpty(nom)) {
            editTextRegisterNom.setError("Nom requis");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editTextRegisterEmail.setError("Email requis");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextRegisterEmail.setError("Email invalide");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextRegisterPassword.setError("Mot de passe requis");
            return;
        }

        if (password.length() < 6) {
            editTextRegisterPassword.setError("Minimum 6 caractères");
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextRegisterConfirmPassword.setError("Les mots de passe ne correspondent pas");
            return;
        }

        showProgress(true);

        // Vérifier si l'email existe déjà
        Utilisateur existant = realm.where(Utilisateur.class)
                .equalTo("email", email)
                .findFirst();

        if (existant != null) {
            showProgress(false);
            Toast.makeText(this, "Cet email est déjà utilisé", Toast.LENGTH_LONG).show();
            return;
        }

        // Créer le nouvel utilisateur
        String userId = java.util.UUID.randomUUID().toString();

        realm.executeTransactionAsync(r -> {
            // Déconnecter tous les utilisateurs existants
            RealmResults<Utilisateur> utilisateursConnectes = r.where(Utilisateur.class)
                    .equalTo("estConnecte", true)
                    .findAll();
            for (Utilisateur u : utilisateursConnectes) {
                u.setEstConnecte(false);
            }

            // Créer le nouvel utilisateur
            Utilisateur nouvelUtilisateur = r.createObject(Utilisateur.class, userId);
            nouvelUtilisateur.setNom(nom);
            nouvelUtilisateur.setEmail(email);
            nouvelUtilisateur.setMotDePasse(password);
            nouvelUtilisateur.setTelephone(telephone);
            nouvelUtilisateur.setFiliere(filiere);
            nouvelUtilisateur.setEstConnecte(true);
        }, () -> {
            showProgress(false);
            Toast.makeText(this, "Inscription réussie !", Toast.LENGTH_LONG).show();

            // Récupérer l'utilisateur créé
            Utilisateur nouvelUtilisateur = realm.where(Utilisateur.class)
                    .equalTo("id", userId)
                    .findFirst();
            startMainActivity(nouvelUtilisateur);
        }, error -> {
            showProgress(false);
            Toast.makeText(this, "Erreur lors de l'inscription: " + error.getMessage(),
                    Toast.LENGTH_LONG).show();
        });
    }

    private void startMainActivity(Utilisateur utilisateur) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("utilisateur_id", utilisateur.getId());
        intent.putExtra("utilisateur_nom", utilisateur.getNom());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonLogin.setEnabled(!show);
        buttonRegister.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}