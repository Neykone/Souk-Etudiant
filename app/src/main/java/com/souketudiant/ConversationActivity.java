package com.souketudiant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.souketudiant.adapters.MessageAdapter;
import com.souketudiant.models.Annonce;
import com.souketudiant.models.Message;
import com.souketudiant.models.Utilisateur;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ConversationActivity extends AppCompatActivity {

    private static final String TAG = "ConversationActivity";

    private Realm realm;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private EditText editTextMessage;
    private MaterialButton buttonEnvoyer;
    private TextView textViewTitre;

    private Annonce annonce;
    private Utilisateur acheteur;
    private Utilisateur vendeur;
    private String annonceId;
    private String vendeurId;
    private String acheteurId;

    private RealmResults<Message> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate démarré");

        try {
            setContentView(R.layout.activity_conversation);
            Log.d(TAG, "Layout chargé");

            // Configuration de la toolbar
            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Conversation");
            }

            // Récupérer les données
            annonceId = getIntent().getStringExtra("annonce_id");
            vendeurId = getIntent().getStringExtra("vendeur_id");
            acheteurId = getIntent().getStringExtra("acheteur_id");

            Log.d(TAG, "annonceId: " + annonceId);
            Log.d(TAG, "vendeurId: " + vendeurId);
            Log.d(TAG, "acheteurId: " + acheteurId);

            if (annonceId == null || vendeurId == null || acheteurId == null) {
                Log.e(TAG, "Paramètres manquants");
                Toast.makeText(this, "Erreur: Paramètres manquants", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialisation Realm
            realm = Realm.getDefaultInstance();
            Log.d(TAG, "Realm initialisé");

            // Charger les données
            chargerDonnees();

            if (annonce == null) {
                Log.e(TAG, "Annonce non trouvée: " + annonceId);
                Toast.makeText(this, "Erreur: Annonce non trouvée", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if (vendeur == null) {
                Log.e(TAG, "Vendeur non trouvé: " + vendeurId);
                Toast.makeText(this, "Erreur: Vendeur non trouvé", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if (acheteur == null) {
                Log.e(TAG, "Acheteur non trouvé: " + acheteurId);
                Toast.makeText(this, "Erreur: Acheteur non trouvé", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Log.d(TAG, "Toutes les données chargées avec succès");
            Log.d(TAG, "Annonce: " + annonce.getTitre());
            Log.d(TAG, "Vendeur: " + vendeur.getNom());
            Log.d(TAG, "Acheteur: " + acheteur.getNom());

            initViews();
            setupRecyclerView();
            chargerMessages();
            setupClickListeners();

        } catch (Exception e) {
            Log.e(TAG, "ERREUR dans onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void chargerDonnees() {
        annonce = realm.where(Annonce.class).equalTo("id", annonceId).findFirst();
        vendeur = realm.where(Utilisateur.class).equalTo("id", vendeurId).findFirst();
        acheteur = realm.where(Utilisateur.class).equalTo("id", acheteurId).findFirst();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonEnvoyer = findViewById(R.id.buttonEnvoyer);
        textViewTitre = findViewById(R.id.textViewTitre);

        textViewTitre.setText("Discussion avec " + vendeur.getNom());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new MessageAdapter(java.util.Collections.emptyList(), acheteurId);
        recyclerView.setAdapter(adapter);
    }

    private void chargerMessages() {
        try {
            Log.d(TAG, "Chargement des messages...");

            // Récupérer les messages
            messages = realm.where(Message.class)
                    .beginGroup()
                    .equalTo("annonce.id", annonceId)
                    .and()
                    .beginGroup()
                    .equalTo("expediteur.id", acheteurId)
                    .or()
                    .equalTo("expediteur.id", vendeurId)
                    .endGroup()
                    .endGroup()
                    .sort("dateEnvoi", Sort.ASCENDING)
                    .findAllAsync();

            // Ajouter un listener pour les changements
            messages.addChangeListener(collection -> {
                Log.d(TAG, "Messages mis à jour: " + collection.size());

                // Copier les messages pour l'adapter (dans le thread UI)
                adapter.updateData(realm.copyFromRealm(collection));

                // Scroll en bas pour voir le dernier message
                if (collection.size() > 0) {
                    recyclerView.scrollToPosition(collection.size() - 1);
                }

                // Marquer les messages comme lus (dans un thread background)
                marquerMessagesCommeLus();
            });

            Log.d(TAG, "Listener ajouté avec succès");

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du chargement des messages: " + e.getMessage(), e);
        }
    }

    private void marquerMessagesCommeLus() {
        // Récupérer les IDs dans le thread UI
        String annonceIdLocal = annonceId;
        String acheteurIdLocal = acheteurId;
        String vendeurIdLocal = vendeurId;

        if (annonceIdLocal == null || acheteurIdLocal == null || vendeurIdLocal == null) {
            Log.e(TAG, "IDs manquants pour marquer les messages comme lus");
            return;
        }

        realm.executeTransactionAsync(r -> {
            // Dans le thread background, on refait une requête propre
            RealmResults<Message> messagesNonLus = r.where(Message.class)
                    .equalTo("annonce.id", annonceIdLocal)
                    .equalTo("estLu", false)
                    .equalTo("destinataire.id", acheteurIdLocal)
                    .findAll();

            for (Message message : messagesNonLus) {
                message.setEstLu(true);
                Log.d(TAG, "Message marqué comme lu: " + message.getId());
            }
        }, () -> {
            Log.d(TAG, "Messages marqués comme lus avec succès");
        }, error -> {
            Log.e(TAG, "Erreur en marquant les messages comme lus: " + error.getMessage(), error);
        });
    }

    private void setupClickListeners() {
        buttonEnvoyer.setOnClickListener(v -> envoyerMessage());
    }

    private void envoyerMessage() {
        String contenu = editTextMessage.getText().toString().trim();

        if (contenu.isEmpty()) {
            Toast.makeText(this, "Veuillez écrire un message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Désactiver le bouton
        buttonEnvoyer.setEnabled(false);

        String messageId = java.util.UUID.randomUUID().toString();
        Date maintenant = new Date();

        // Récupérer les IDs dans le thread UI
        String annonceIdLocal = annonceId;
        String acheteurIdLocal = acheteurId;
        String vendeurIdLocal = vendeurId;

        Log.d(TAG, "Envoi du message: " + contenu);

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                // Dans le thread background, on récupère les objets à nouveau
                Annonce bgAnnonce = bgRealm.where(Annonce.class).equalTo("id", annonceIdLocal).findFirst();
                Utilisateur bgExpediteur = bgRealm.where(Utilisateur.class).equalTo("id", acheteurIdLocal).findFirst();
                Utilisateur bgDestinataire = bgRealm.where(Utilisateur.class).equalTo("id", vendeurIdLocal).findFirst();

                if (bgAnnonce != null && bgExpediteur != null && bgDestinataire != null) {
                    Message message = bgRealm.createObject(Message.class, messageId);
                    message.setContenu(contenu);
                    message.setAnnonce(bgAnnonce);
                    message.setExpediteur(bgExpediteur);
                    message.setDestinataire(bgDestinataire);
                    message.setDateEnvoi(maintenant);
                    message.setEstLu(false);

                    Log.d(TAG, "Message créé avec ID: " + messageId);
                } else {
                    Log.e(TAG, "Erreur: objets non trouvés dans la transaction");
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Message envoyé avec succès");
                editTextMessage.setText("");
                buttonEnvoyer.setEnabled(true);
                Toast.makeText(ConversationActivity.this, "Message envoyé", Toast.LENGTH_SHORT).show();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.e(TAG, "Erreur lors de l'envoi: " + error.getMessage(), error);
                buttonEnvoyer.setEnabled(true);
                Toast.makeText(ConversationActivity.this, "Erreur lors de l'envoi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "onSupportNavigateUp");
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        // Nettoyer les listeners
        if (messages != null) {
            messages.removeAllChangeListeners();
        }
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}