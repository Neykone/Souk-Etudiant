package com.souketudiant.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.souketudiant.DetailAnnonceActivity;
import com.souketudiant.R;
import com.souketudiant.models.Annonce;
import com.souketudiant.models.Utilisateur;

import io.realm.Realm;
import io.realm.RealmResults;

public class ProfilFragment extends Fragment {

    private Realm realm;
    private TextView textViewNom;
    private TextView textViewEmail;
    private TextView textViewFiliere;
    private MaterialButton buttonMesAnnonces;
    private MaterialButton buttonFavoris;
    private MaterialButton buttonDeconnexion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        realm = Realm.getDefaultInstance();

        initViews(view);
        chargerProfil();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        textViewNom = view.findViewById(R.id.textViewNom);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewFiliere = view.findViewById(R.id.textViewFiliere);
        buttonMesAnnonces = view.findViewById(R.id.buttonMesAnnonces);
        buttonFavoris = view.findViewById(R.id.buttonFavoris);
        buttonDeconnexion = view.findViewById(R.id.buttonDeconnexion);
    }

    private void chargerProfil() {
        Utilisateur utilisateur = realm.where(Utilisateur.class).findFirst();

        if (utilisateur != null) {
            textViewNom.setText(utilisateur.getNom());
            textViewEmail.setText(utilisateur.getEmail());
            textViewFiliere.setText(utilisateur.getFiliere());
        }
    }

    private void setupClickListeners() {
        buttonMesAnnonces.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Voir mes annonces (à venir)", Toast.LENGTH_SHORT).show();
        });

        buttonFavoris.setOnClickListener(v -> {
            // Ouvrir la liste des favoris
            showFavorisDialog();
        });

        buttonDeconnexion.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Déconnexion (à venir)", Toast.LENGTH_SHORT).show();
        });
    }

    private void showFavorisDialog() {
        RealmResults<Annonce> favoris = realm.where(Annonce.class)
                .equalTo("estFavori", true)
                .findAll();

        if (favoris.isEmpty()) {
            Toast.makeText(getContext(),
                    "Vous n'avez pas encore d'annonces favorites",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Créer une liste des titres des favoris
        String[] titresFavoris = new String[favoris.size()];
        for (int i = 0; i < favoris.size(); i++) {
            titresFavoris[i] = favoris.get(i).getTitre() +
                    " - " + favoris.get(i).getPrix() + "€";
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Mes favoris")
                .setItems(titresFavoris, (dialog, which) -> {
                    // Ouvrir le détail de l'annonce sélectionnée
                    Annonce annonce = favoris.get(which);
                    Intent intent = new Intent(getActivity(), DetailAnnonceActivity.class);
                    intent.putExtra("annonce_id", annonce.getId());
                    startActivity(intent);
                })
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (realm != null) {
            realm.close();
        }
    }
}