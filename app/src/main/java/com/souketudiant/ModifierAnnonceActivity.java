package com.souketudiant;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.souketudiant.models.Annonce;
import com.souketudiant.utils.Categories;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.realm.Realm;

public class ModifierAnnonceActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

    private Realm realm;
    private Annonce annonce;
    private String annonceId;

    private ImageView imageViewApercu;
    private TextInputEditText editTextTitre;
    private TextInputEditText editTextPrix;
    private TextInputEditText editTextDescription;
    private AutoCompleteTextView autoCompleteCategorie;
    private AutoCompleteTextView autoCompleteEtat;
    private MaterialButton buttonPrendrePhoto;
    private MaterialButton buttonChoisirGaleries;
    private MaterialButton buttonEnregistrer;
    private MaterialButton buttonAnnuler;

    private String photoBase64 = "";
    private boolean imageModifiee = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifier_annonce);

        // Configuration de la toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Modifier l'annonce");

        // Récupérer l'ID de l'annonce
        annonceId = getIntent().getStringExtra("annonce_id");
        if (annonceId == null) {
            Toast.makeText(this, "Erreur: Annonce non trouvée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialisation Realm
        realm = Realm.getDefaultInstance();
        annonce = realm.where(Annonce.class).equalTo("id", annonceId).findFirst();

        if (annonce == null) {
            Toast.makeText(this, "Erreur: Annonce non trouvée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupSpinners();
        remplirChamps();
        setupClickListeners();
    }

    private void initViews() {
        imageViewApercu = findViewById(R.id.imageViewApercu);
        editTextTitre = findViewById(R.id.editTextTitre);
        editTextPrix = findViewById(R.id.editTextPrix);
        editTextDescription = findViewById(R.id.editTextDescription);
        autoCompleteCategorie = findViewById(R.id.autoCompleteCategorie);
        autoCompleteEtat = findViewById(R.id.autoCompleteEtat);
        buttonPrendrePhoto = findViewById(R.id.buttonPrendrePhoto);
        buttonChoisirGaleries = findViewById(R.id.buttonChoisirGaleries);
        buttonEnregistrer = findViewById(R.id.buttonEnregistrer);
        buttonAnnuler = findViewById(R.id.buttonAnnuler);
    }

    private void setupSpinners() {
        // Catégories
        ArrayAdapter<String> categorieAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                Categories.CATEGORIES
        );
        autoCompleteCategorie.setAdapter(categorieAdapter);

        // États
        ArrayAdapter<String> etatAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                Categories.ETATS
        );
        autoCompleteEtat.setAdapter(etatAdapter);
    }

    private void remplirChamps() {
        editTextTitre.setText(annonce.getTitre());
        editTextPrix.setText(String.valueOf(annonce.getPrix()));
        editTextDescription.setText(annonce.getDescription());
        autoCompleteCategorie.setText(annonce.getCategorie(), false);
        autoCompleteEtat.setText(annonce.getEtat(), false);

        // Charger l'image existante
        if (annonce.getPhotoUrl() != null && !annonce.getPhotoUrl().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(annonce.getPhotoUrl(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageViewApercu.setImageBitmap(decodedByte);
                photoBase64 = annonce.getPhotoUrl();
            } catch (Exception e) {
                imageViewApercu.setImageResource(R.drawable.ic_book_placeholder);
            }
        } else {
            imageViewApercu.setImageResource(R.drawable.ic_book_placeholder);
        }
    }

    private void setupClickListeners() {
        buttonPrendrePhoto.setOnClickListener(v -> prendrePhoto());
        buttonChoisirGaleries.setOnClickListener(v -> choisirDansGaleries());
        buttonEnregistrer.setOnClickListener(v -> enregistrerModifications());
        buttonAnnuler.setOnClickListener(v -> finish());
    }

    private void prendrePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Pas d'application appareil photo disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void choisirDansGaleries() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            try {
                Bitmap bitmap = null;

                if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    Bundle extras = data.getExtras();
                    bitmap = (Bitmap) extras.get("data");
                } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                    Uri selectedImage = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                }

                if (bitmap != null) {
                    // Redimensionner
                    bitmap = resizeBitmap(bitmap, 300, 300);
                    imageViewApercu.setImageBitmap(bitmap);
                    photoBase64 = bitmapToBase64(bitmap);
                    imageModifiee = true;
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap resizeBitmap(Bitmap image, int maxWidth, int maxHeight) {
        if (image.getHeight() > maxHeight || image.getWidth() > maxWidth) {
            float ratio = Math.min(
                    (float) maxWidth / image.getWidth(),
                    (float) maxHeight / image.getHeight());

            int width = Math.round((float) image.getWidth() * ratio);
            int height = Math.round((float) image.getHeight() * ratio);

            return Bitmap.createScaledBitmap(image, width, height, true);
        }
        return image;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void enregistrerModifications() {
        // Validation des champs
        String titre = editTextTitre.getText().toString().trim();
        String prixStr = editTextPrix.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String categorie = autoCompleteCategorie.getText().toString().trim();
        String etat = autoCompleteEtat.getText().toString().trim();

        if (titre.isEmpty()) {
            editTextTitre.setError("Le titre est requis");
            return;
        }

        if (prixStr.isEmpty()) {
            editTextPrix.setError("Le prix est requis");
            return;
        }

        double prix;
        try {
            prix = Double.parseDouble(prixStr);
        } catch (NumberFormatException e) {
            editTextPrix.setError("Prix invalide");
            return;
        }

        if (categorie.isEmpty()) {
            autoCompleteCategorie.setError("La catégorie est requise");
            return;
        }

        if (etat.isEmpty()) {
            autoCompleteEtat.setError("L'état est requis");
            return;
        }

        // Confirmation
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Voulez-vous enregistrer les modifications ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    sauvegarderModifications(titre, prix, description, categorie, etat);
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void sauvegarderModifications(String titre, double prix, String description,
                                          String categorie, String etat) {
        realm.executeTransactionAsync(r -> {
            Annonce a = r.where(Annonce.class).equalTo("id", annonceId).findFirst();
            if (a != null) {
                a.setTitre(titre);
                a.setPrix(prix);
                a.setDescription(description);
                a.setCategorie(categorie);
                a.setEtat(etat);

                if (imageModifiee) {
                    a.setPhotoUrl(photoBase64);
                    a.setAPhoto(!photoBase64.isEmpty());
                }

                a.setDateModification(new java.util.Date());
            }
        }, () -> {
            Toast.makeText(this, "Annonce modifiée avec succès!", Toast.LENGTH_LONG).show();
            finish();
        }, error -> {
            Toast.makeText(this, "Erreur lors de la modification: " + error.getMessage(),
                    Toast.LENGTH_LONG).show();
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
