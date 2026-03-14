package com.souketudiant;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.souketudiant.models.Annonce;

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

        // Initialiser les vues
        initViews();

        // Afficher les détails
        afficherDetails();

        // Configurer les boutons
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
        // Titre
        textViewTitre.setText(annonce.getTitre());

        // Prix
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        textViewPrix.setText(format.format(annonce.getPrix()));

        // Catégorie et état
        textViewCategorie.setText(annonce.getCategorie());
        textViewEtat.setText(annonce.getEtat());

        // Description
        if (annonce.getDescription() != null && !annonce.getDescription().isEmpty()) {
            textViewDescription.setText(annonce.getDescription());
        } else {
            textViewDescription.setText("Aucune description fournie");
        }

        // Vendeur
        if (annonce.getVendeur() != null) {
            textViewVendeur.setText(annonce.getVendeur().getNom());
        } else {
            textViewVendeur.setText("Vendeur inconnu");
        }

        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        textViewDate.setText("Publié le " + sdf.format(annonce.getDatePublication()));

        // Image
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
    }

    private void setupButtons() {
        buttonContacter.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Fonctionnalité de contact à venir",
                    Toast.LENGTH_SHORT).show();
        });

        buttonFavoris.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Annonce ajoutée aux favoris",
                    Toast.LENGTH_SHORT).show();
            // TODO: Implémenter la logique des favoris
        });

        buttonRetour.setOnClickListener(v -> {
            finish(); // Retour à l'écran précédent
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}