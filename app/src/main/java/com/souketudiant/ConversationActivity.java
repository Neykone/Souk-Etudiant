package com.souketudiant;

import android.os.Bundle;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // Configuration de la toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Récupérer les données
        annonceId = getIntent().getStringExtra("annonce_id");
        vendeurId = getIntent().getStringExtra("vendeur_id");

        if (annonceId == null || vendeurId == null) {
            Toast.makeText(this, "Erreur: Conversation impossible", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        realm = Realm.getDefaultInstance();

        // Charger l'annonce et les utilisateurs
        annonce = realm.where(Annonce.class).equalTo("id", annonceId).findFirst();
        vendeur = realm.where(Utilisateur.class).equalTo("id", vendeurId).findFirst();

        // Pour la démo, l'acheteur est l'utilisateur connecté (premier utilisateur)
        acheteur = realm.where(Utilisateur.class).findFirst();

        if (annonce == null || vendeur == null || acheteur == null) {
            Toast.makeText(this, "Erreur: Données manquantes", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        chargerMessages();
        setupClickListeners();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonEnvoyer = findViewById(R.id.buttonEnvoyer);
        textViewTitre = findViewById(R.id.textViewTitre);

        getSupportActionBar().setTitle(annonce.getTitre());
        textViewTitre.setText("Discussion avec " + vendeur.getNom());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new MessageAdapter(java.util.Collections.emptyList(), acheteur.getId());
        recyclerView.setAdapter(adapter);
    }

    private void chargerMessages() {
        RealmResults<Message> messages = realm.where(Message.class)
                .beginGroup()
                .equalTo("annonce.id", annonceId)
                .and()
                .beginGroup()
                .equalTo("expediteur.id", acheteur.getId())
                .or()
                .equalTo("expediteur.id", vendeur.getId())
                .endGroup()
                .endGroup()
                .sort("dateEnvoi", Sort.ASCENDING)
                .findAllAsync();

        messages.addChangeListener(collection -> {
            adapter.updateData(realm.copyFromRealm(collection));
            // Scroll en bas pour voir le dernier message
            if (collection.size() > 0) {
                recyclerView.scrollToPosition(collection.size() - 1);
            }

            // Marquer les messages comme lus
            marquerMessagesCommeLus(collection);
        });
    }

    private void marquerMessagesCommeLus(RealmResults<Message> messages) {
        realm.executeTransactionAsync(r -> {
            for (Message message : messages) {
                if (!message.isEstLu() &&
                        message.getDestinataire() != null &&
                        message.getDestinataire().getId().equals(acheteur.getId())) {
                    message.setEstLu(true);
                }
            }
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

        String messageId = java.util.UUID.randomUUID().toString();

        realm.executeTransactionAsync(r -> {
            Message message = r.createObject(Message.class, messageId);
            message.setContenu(contenu);
            message.setAnnonce(annonce);
            message.setExpediteur(acheteur);
            message.setDestinataire(vendeur);
            message.setDateEnvoi(new Date());
            message.setEstLu(false);
        }, () -> {
            editTextMessage.setText("");
        }, error -> {
            Toast.makeText(this, "Erreur lors de l'envoi", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}
