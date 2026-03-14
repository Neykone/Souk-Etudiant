package com.souketudiant.fragments;

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
import com.souketudiant.R;
import com.souketudiant.models.Utilisateur;

import io.realm.Realm;

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
            Toast.makeText(getContext(), "Voir mes annonces", Toast.LENGTH_SHORT).show();
        });

        buttonFavoris.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Voir mes favoris", Toast.LENGTH_SHORT).show();
        });

        buttonDeconnexion.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Déconnexion", Toast.LENGTH_SHORT).show();
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