package com.souketudiant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.souketudiant.adapters.MessageAdapter;
import com.souketudiant.models.Annonce;
import com.souketudiant.models.Message;
import com.souketudiant.models.Utilisateur;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ConversationActivity extends AppCompatActivity {

    private static final String TAG = "ConversationActivity";
    private static final int PERMISSION_LOCATION_CODE = 500;

    private Realm realm;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private EditText editTextMessage;
    private MaterialButton buttonEnvoyer;
    private MaterialButton buttonPartagerLocalisation;
    private TextView textViewTitre;

    private Annonce annonce;
    private Utilisateur acheteur;
    private Utilisateur vendeur;
    private String annonceId;
    private String vendeurId;
    private String acheteurId;

    private RealmResults<Message> messages;
    private FusedLocationProviderClient fusedLocationClient;

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

            // Initialiser le client de localisation
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
        buttonPartagerLocalisation = findViewById(R.id.buttonPartagerLocalisation);
        textViewTitre = findViewById(R.id.textViewTitre);

        textViewTitre.setText("Discussion avec " + vendeur.getNom());

        // Vérifier que le bouton n'est pas null
        if (buttonPartagerLocalisation != null) {
            buttonPartagerLocalisation.setOnClickListener(v -> partagerLocalisation());
        } else {
            Log.e(TAG, "buttonPartagerLocalisation est null !");
        }
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

            // IMPORTANT: Récupérer TOUS les messages de cette conversation
            messages = realm.where(Message.class)
                    .equalTo("annonce.id", annonceId)  // Même annonce
                    .beginGroup()
                    .equalTo("expediteur.id", acheteurId)
                    .or()
                    .equalTo("expediteur.id", vendeurId)
                    .or()
                    .equalTo("destinataire.id", acheteurId)
                    .or()
                    .equalTo("destinataire.id", vendeurId)
                    .endGroup()
                    .sort("dateEnvoi", Sort.ASCENDING)
                    .findAllAsync();

            // Ajouter un listener pour les changements
            messages.addChangeListener(collection -> {
                Log.d(TAG, "Messages mis à jour: " + collection.size());

                // Afficher tous les messages dans l'ordre chronologique
                adapter.updateData(realm.copyFromRealm(collection));

                // Scroll en bas pour voir le dernier message
                if (collection.size() > 0) {
                    recyclerView.scrollToPosition(collection.size() - 1);
                }

                // Marquer les messages comme lus
                marquerMessagesCommeLus();
            });

            Log.d(TAG, "Listener ajouté avec succès");

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du chargement des messages: " + e.getMessage(), e);
        }
    }

    private void marquerMessagesCommeLus() {
        String annonceIdLocal = annonceId;
        String acheteurIdLocal = acheteurId;

        if (annonceIdLocal == null || acheteurIdLocal == null) {
            Log.e(TAG, "IDs manquants pour marquer les messages comme lus");
            return;
        }

        realm.executeTransactionAsync(r -> {
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
        buttonPartagerLocalisation.setOnClickListener(v -> partagerLocalisation());
    }

    private void envoyerMessage() {
        String contenu = editTextMessage.getText().toString().trim();

        if (contenu.isEmpty()) {
            Toast.makeText(this, "Veuillez écrire un message", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonEnvoyer.setEnabled(false);

        String messageId = java.util.UUID.randomUUID().toString();
        Date maintenant = new Date();

        String annonceIdLocal = annonceId;
        String acheteurIdLocal = acheteurId;
        String vendeurIdLocal = vendeurId;

        Log.d(TAG, "Envoi du message: " + contenu);

        realm.executeTransactionAsync(r -> {
            Annonce bgAnnonce = r.where(Annonce.class).equalTo("id", annonceIdLocal).findFirst();
            Utilisateur bgExpediteur = r.where(Utilisateur.class).equalTo("id", acheteurIdLocal).findFirst();
            Utilisateur bgDestinataire = r.where(Utilisateur.class).equalTo("id", vendeurIdLocal).findFirst();

            if (bgAnnonce != null && bgExpediteur != null && bgDestinataire != null) {
                Message message = r.createObject(Message.class, messageId);
                message.setType("text");
                message.setContenu(contenu);
                message.setAnnonce(bgAnnonce);
                message.setExpediteur(bgExpediteur);
                message.setDestinataire(bgDestinataire);
                message.setDateEnvoi(maintenant);
                message.setEstLu(false);
            }
        }, () -> {
            Log.d(TAG, "Message envoyé avec succès");
            editTextMessage.setText("");
            buttonEnvoyer.setEnabled(true);
        }, error -> {
            Log.e(TAG, "Erreur lors de l'envoi: " + error.getMessage(), error);
            buttonEnvoyer.setEnabled(true);
            Toast.makeText(ConversationActivity.this, "Erreur lors de l'envoi", Toast.LENGTH_SHORT).show();
        });
    }

    private void partagerLocalisation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION_CODE);
            return;
        }

        obtenirLocalisation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_LOCATION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenirLocalisation();
            } else {
                Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void obtenirLocalisation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        buttonPartagerLocalisation.setEnabled(false);
        Toast.makeText(this, "Obtention de la position...", Toast.LENGTH_SHORT).show();

        Task<Location> locationTask = fusedLocationClient.getLastLocation();
        locationTask.addOnSuccessListener(location -> {
            buttonPartagerLocalisation.setEnabled(true);

            if (location != null) {
                String lieuNom = getLieuName(location.getLatitude(), location.getLongitude());
                envoyerLocalisation(location.getLatitude(), location.getLongitude(), lieuNom);
            } else {
                Toast.makeText(this,
                        "Impossible d'obtenir la position. Vérifiez que le GPS est activé.",
                        Toast.LENGTH_LONG).show();
            }
        });

        locationTask.addOnFailureListener(e -> {
            buttonPartagerLocalisation.setEnabled(true);
            Toast.makeText(this, "Erreur de localisation: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private String getLieuName(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                if (address.getThoroughfare() != null) {
                    sb.append(address.getThoroughfare());
                }
                if (address.getLocality() != null) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(address.getLocality());
                }
                return sb.length() > 0 ? sb.toString() : "Position partagée";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Position partagée";
    }

    private void envoyerLocalisation(double latitude, double longitude, String lieuNom) {
        String messageId = java.util.UUID.randomUUID().toString();
        Date maintenant = new Date();

        String annonceIdLocal = annonceId;
        String acheteurIdLocal = acheteurId;
        String vendeurIdLocal = vendeurId;

        realm.executeTransactionAsync(r -> {
            Annonce bgAnnonce = r.where(Annonce.class).equalTo("id", annonceIdLocal).findFirst();
            Utilisateur bgExpediteur = r.where(Utilisateur.class).equalTo("id", acheteurIdLocal).findFirst();
            Utilisateur bgDestinataire = r.where(Utilisateur.class).equalTo("id", vendeurIdLocal).findFirst();

            if (bgAnnonce != null && bgExpediteur != null && bgDestinataire != null) {
                Message message = r.createObject(Message.class, messageId);
                message.setType("location");  // IMPORTANT: bien mettre le type
                message.setLatitude(latitude);
                message.setLongitude(longitude);
                message.setLieuNom(lieuNom);
                message.setContenu(null);    // Pas de contenu texte
                message.setAnnonce(bgAnnonce);
                message.setExpediteur(bgExpediteur);
                message.setDestinataire(bgDestinataire);
                message.setDateEnvoi(maintenant);
                message.setEstLu(false);
            }
        }, () -> {
            Toast.makeText(ConversationActivity.this,
                    "📍 Position partagée avec succès", Toast.LENGTH_SHORT).show();
        }, error -> {
            Toast.makeText(ConversationActivity.this,
                    "Erreur lors du partage de position", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_OK);
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messages != null) {
            messages.removeAllChangeListeners();
        }
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}