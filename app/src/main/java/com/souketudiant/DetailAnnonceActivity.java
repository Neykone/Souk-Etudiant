package com.souketudiant;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.souketudiant.models.Annonce;
import com.souketudiant.models.Utilisateur;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.realm.Realm;

public class DetailAnnonceActivity extends AppCompatActivity {

    private Realm realm;
    private Annonce annonce;

    private ImageView imageView;
    private TextView textViewTitre;
    private TextView textViewPrix;
    private TextView textViewCategorie;
    private TextView textViewEtat;
    private TextView textViewDescription;
    private TextView textViewVendeur;
    private TextView textViewDate;
    private MaterialButton buttonContacter;
    private MaterialButton buttonFavoris;
    private MaterialButton buttonRetour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_annonce);

        // Configuration de la toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Détail de l'annonce");
        }

        // Initialisation Realm
        realm = Realm.getDefaultInstance();

        // Récupérer l'ID de l'annonce passée en paramètre
        String annonceId = getIntent().getStringExtra("annonce_id");

        if (annonceId == null) {
            Toast.makeText(this, "Erreur: Annonce non trouvée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Charger l'annonce
        annonce = realm.where(Annonce.class).equalTo("id", annonceId).findFirst();

        if (annonce == null) {
            Toast.makeText(this, "Erreur: Annonce non trouvée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        afficherDetails();
        setupButtons();
    }

    private void initViews() {
        imageView = findViewById(R.id.imageViewDetail);
        textViewTitre = findViewById(R.id.textViewDetailTitre);
        textViewPrix = findViewById(R.id.textViewDetailPrix);
        textViewCategorie = findViewById(R.id.textViewDetailCategorie);
        textViewEtat = findViewById(R.id.textViewDetailEtat);
        textViewDescription = findViewById(R.id.textViewDetailDescription);
        textViewVendeur = findViewById(R.id.textViewDetailVendeur);
        textViewDate = findViewById(R.id.textViewDetailDate);
        buttonContacter = findViewById(R.id.buttonContacter);
        buttonFavoris = findViewById(R.id.buttonFavoris);
        buttonRetour = findViewById(R.id.buttonRetour);
    }

    private void afficherDetails() {
        textViewTitre.setText(annonce.getTitre());

        // Prix
        String prixFormate = String.format(Locale.FRANCE, "%.2f DH", annonce.getPrix());
        textViewPrix.setText(prixFormate);

        textViewCategorie.setText(annonce.getCategorie());
        textViewEtat.setText(annonce.getEtat());

        if (annonce.getDescription() != null && !annonce.getDescription().isEmpty()) {
            textViewDescription.setText(annonce.getDescription());
        } else {
            textViewDescription.setText("Aucune description fournie");
        }

        if (annonce.getVendeur() != null) {
            textViewVendeur.setText(annonce.getVendeur().getNom());
        } else {
            textViewVendeur.setText("Vendeur inconnu");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        textViewDate.setText("Publié le " + sdf.format(annonce.getDatePublication()));

        if (annonce.getPhotoUrl() != null && !annonce.getPhotoUrl().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(annonce.getPhotoUrl(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView.setImageBitmap(decodedByte);
            } catch (Exception e) {
                imageView.setImageResource(R.drawable.ic_book_placeholder);
            }
        } else {
            imageView.setImageResource(R.drawable.ic_book_placeholder);
        }

        updateFavoriButtonText();
    }

    private void setupButtons() {
        buttonContacter.setOnClickListener(v -> {
            try {
                if (annonce == null) {
                    Toast.makeText(this, "Annonce non disponible", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (annonce.getVendeur() == null) {
                    Toast.makeText(this, "Vendeur non identifié", Toast.LENGTH_SHORT).show();
                    return;
                }

                Utilisateur utilisateurConnecte = realm.where(Utilisateur.class)
                        .equalTo("estConnecte", true)
                        .findFirst();

                if (utilisateurConnecte == null) {
                    Toast.makeText(this, "Vous devez être connecté", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (utilisateurConnecte.getId().equals(annonce.getVendeur().getId())) {
                    new AlertDialog.Builder(this)
                            .setTitle("Information")
                            .setMessage("C'est votre propre annonce. Vous ne pouvez pas vous contacter vous-même.")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                Log.d("DetailAnnonce", "Annonce ID: " + annonce.getId());
                Log.d("DetailAnnonce", "Vendeur ID: " + annonce.getVendeur().getId());
                Log.d("DetailAnnonce", "Acheteur ID: " + utilisateurConnecte.getId());

                Intent intent = new Intent(this, ConversationActivity.class);
                intent.putExtra("annonce_id", annonce.getId());
                intent.putExtra("vendeur_id", annonce.getVendeur().getId());
                intent.putExtra("acheteur_id", utilisateurConnecte.getId());
                startActivityForResult(intent, 600);

            } catch (Exception e) {
                Log.e("DetailAnnonce", "Erreur: " + e.getMessage(), e);
                Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        buttonFavoris.setOnClickListener(v -> toggleFavori());

        // ✅ CORRECTION : setResult(RESULT_OK) avant finish()
        buttonRetour.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });
    }

    private void toggleFavori() {
        String annonceId = annonce.getId();

        realm.executeTransactionAsync(r -> {
            Utilisateur user = r.where(Utilisateur.class)
                    .equalTo("estConnecte", true)
                    .findFirst();
            if (user == null) return;

            if (user.getAnnoncesFavorisIds().contains(annonceId)) {
                user.getAnnoncesFavorisIds().remove(annonceId);
            } else {
                user.getAnnoncesFavorisIds().add(annonceId);
            }
        }, () -> {
            Toast.makeText(this, "Favoris mis à jour", Toast.LENGTH_SHORT).show();
            updateFavoriButtonText();
        }, error -> {
            Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateFavoriButtonText() {
        Utilisateur user = realm.where(Utilisateur.class)
                .equalTo("estConnecte", true)
                .findFirst();
        boolean isFavori = user != null
                && user.getAnnoncesFavorisIds() != null
                && user.getAnnoncesFavorisIds().contains(annonce.getId());
        buttonFavoris.setText(isFavori ? "Retirer des favoris" : "Ajouter aux favoris");
    }


    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_OK);
        finish();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Retour de ConversationActivity — rien à faire ici
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}