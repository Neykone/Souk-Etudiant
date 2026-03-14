package com.souketudiant.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.souketudiant.R;
import com.souketudiant.models.Annonce;
import com.souketudiant.models.Utilisateur;
import com.souketudiant.utils.Categories;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.realm.Realm;

public class PublierFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

    private Realm realm;
    private ImageView imageViewApercu;
    private TextInputEditText editTextTitre;
    private TextInputEditText editTextPrix;
    private TextInputEditText editTextDescription;
    private AutoCompleteTextView autoCompleteCategorie;
    private AutoCompleteTextView autoCompleteEtat;
    private MaterialButton buttonPublier;

    private String photoBase64 = "";
    private Utilisateur utilisateurCourant;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publier, container, false);

        realm = Realm.getDefaultInstance();

        // Récupérer ou créer un utilisateur pour la démo
        utilisateurCourant = getUtilisateurTest();

        initViews(view);
        setupSpinners();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        imageViewApercu = view.findViewById(R.id.imageViewApercu);
        editTextTitre = view.findViewById(R.id.editTextTitre);
        editTextPrix = view.findViewById(R.id.editTextPrix);
        editTextDescription = view.findViewById(R.id.editTextDescription);
        autoCompleteCategorie = view.findViewById(R.id.autoCompleteCategorie);
        autoCompleteEtat = view.findViewById(R.id.autoCompleteEtat);
        buttonPublier = view.findViewById(R.id.buttonPublier);

        MaterialButton buttonPrendrePhoto = view.findViewById(R.id.buttonPrendrePhoto);
        MaterialButton buttonChoisirGaleries = view.findViewById(R.id.buttonChoisirGaleries);

        buttonPrendrePhoto.setOnClickListener(v -> prendrePhoto());
        buttonChoisirGaleries.setOnClickListener(v -> choisirDansGaleries());
    }

    private void setupSpinners() {
        // Catégories
        ArrayAdapter<String> categorieAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                Categories.CATEGORIES
        );
        autoCompleteCategorie.setAdapter(categorieAdapter);

        // États
        ArrayAdapter<String> etatAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                Categories.ETATS
        );
        autoCompleteEtat.setAdapter(etatAdapter);
    }

    private void setupClickListeners() {
        buttonPublier.setOnClickListener(v -> publierAnnonce());
    }

    private void prendrePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(getContext(), "Pas d'application appareil photo disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void choisirDansGaleries() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            try {
                Bitmap bitmap = null;

                if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    // Photo de l'appareil photo
                    Bundle extras = data.getExtras();
                    bitmap = (Bitmap) extras.get("data");
                } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                    // Photo de la galerie
                    Uri selectedImage = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImage);
                }

                if (bitmap != null) {
                    // Redimensionner pour éviter les problèmes de mémoire
                    bitmap = resizeBitmap(bitmap, 300, 300);

                    // Afficher l'aperçu
                    imageViewApercu.setImageBitmap(bitmap);

                    // Convertir en Base64 pour stockage
                    photoBase64 = bitmapToBase64(bitmap);
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show();
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

    private Utilisateur getUtilisateurTest() {
        // Important: findFirst() retourne un objet géré par Realm
        Utilisateur utilisateur = realm.where(Utilisateur.class).findFirst();

        if (utilisateur == null) {
            // Créer un utilisateur de test dans une transaction
            realm.executeTransaction(r -> {
                String userId = java.util.UUID.randomUUID().toString();
                Utilisateur u = r.createObject(Utilisateur.class, userId);
                u.setNom("Étudiant Test");
                u.setEmail("test@etudiant.univ.fr");
                u.setTelephone("0123456789");
                u.setFiliere("Informatique");
            });
            // Récupérer l'utilisateur fraîchement créé
            utilisateur = realm.where(Utilisateur.class).findFirst();
        }

        return utilisateur; // Cet objet est attaché au Realm
    }

    private void publierAnnonce() {
        // Validation des champs (inchangée)
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

        // Récupérer l'ID de l'utilisateur (pas l'objet entier)
        String utilisateurId = utilisateurCourant.getId();

        // Boîte de dialogue de confirmation
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Voulez-vous publier cette annonce ?")
                .setPositiveButton("Publier", (dialog, which) -> {
                    sauvegarderAnnonce(titre, prix, description, categorie, etat, utilisateurId);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void sauvegarderAnnonce(String titre, double prix, String description,
                                    String categorie, String etat, String utilisateurId) {
        String annonceId = java.util.UUID.randomUUID().toString();

        realm.executeTransactionAsync(r -> {
            // Récupérer l'utilisateur dans cette transaction
            Utilisateur vendeur = r.where(Utilisateur.class)
                    .equalTo("id", utilisateurId)
                    .findFirst();

            if (vendeur != null) {
                Annonce annonce = r.createObject(Annonce.class, annonceId);
                annonce.setTitre(titre);
                annonce.setPrix(prix);
                annonce.setDescription(description);
                annonce.setCategorie(categorie);
                annonce.setEtat(etat);
                annonce.setVendeur(vendeur);  // Maintenant vendeur est dans la même transaction
                annonce.setPhotoUrl(photoBase64);
                annonce.setAPhoto(!photoBase64.isEmpty());
            }
        }, () -> {
            // Succès
            Toast.makeText(getContext(), "Annonce publiée avec succès!", Toast.LENGTH_LONG).show();

            // Réinitialiser le formulaire (sur le thread UI)
            requireActivity().runOnUiThread(() -> {
                editTextTitre.setText("");
                editTextPrix.setText("");
                editTextDescription.setText("");
                autoCompleteCategorie.setText("");
                autoCompleteEtat.setText("");
                imageViewApercu.setImageResource(R.drawable.ic_book_placeholder);
                photoBase64 = "";
            });

        }, error -> {
            // Erreur
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(),
                        "Erreur lors de la publication: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (realm != null) {
            realm.close();
        }
    }
}
