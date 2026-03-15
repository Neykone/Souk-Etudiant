package com.souketudiant.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.souketudiant.ConversationsActivity;
import com.souketudiant.LoginActivity;
import com.souketudiant.MesAnnoncesActivity;
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
    private TextView textViewNbAnnonces;
    private TextView textViewNbFavoris;
    private TextView textViewNbVendues;
    private MaterialButton buttonMesAnnonces;
    private MaterialButton buttonFavoris;
    private MaterialButton buttonConversations;
    private MaterialButton buttonDeconnexion;

    private Utilisateur utilisateurCourant;
    private RealmResults<Annonce> mesAnnonces;
    private RealmResults<Annonce> mesFavoris;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        realm = Realm.getDefaultInstance();

        // Récupérer l'utilisateur connecté
        utilisateurCourant = realm.where(Utilisateur.class)
                .equalTo("estConnecte", true)
                .findFirst();

        initViews(view);

        if (utilisateurCourant != null) {
            chargerProfil();
            chargerStatistiques();
        }

        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        textViewNom = view.findViewById(R.id.textViewNom);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewFiliere = view.findViewById(R.id.textViewFiliere);
        textViewNbAnnonces = view.findViewById(R.id.textViewNbAnnonces);
        textViewNbFavoris = view.findViewById(R.id.textViewNbFavoris);
        textViewNbVendues = view.findViewById(R.id.textViewNbVendues);
        buttonMesAnnonces = view.findViewById(R.id.buttonMesAnnonces);
        buttonFavoris = view.findViewById(R.id.buttonFavoris);
        buttonConversations = view.findViewById(R.id.buttonConversations);
        buttonDeconnexion = view.findViewById(R.id.buttonDeconnexion);
    }

    private void chargerProfil() {
        textViewNom.setText(utilisateurCourant.getNom());
        textViewEmail.setText(utilisateurCourant.getEmail());
        textViewFiliere.setText(utilisateurCourant.getFiliere() != null ?
                utilisateurCourant.getFiliere() : "Non renseignée");
    }

    private void chargerStatistiques() {
        // Mes annonces
        mesAnnonces = realm.where(Annonce.class)
                .equalTo("vendeur.id", utilisateurCourant.getId())
                .findAllAsync();

        mesAnnonces.addChangeListener(collection -> {
            textViewNbAnnonces.setText(String.valueOf(collection.size()));

            long vendues = collection.where().equalTo("estVendu", true).count();
            textViewNbVendues.setText(String.valueOf(vendues));
        });

        // Mes favoris
        mesFavoris = realm.where(Annonce.class)
                .equalTo("estFavori", true)
                .findAllAsync();

        mesFavoris.addChangeListener(collection -> {
            textViewNbFavoris.setText(String.valueOf(collection.size()));
        });
    }

    private void setupClickListeners() {
        buttonMesAnnonces.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MesAnnoncesActivity.class);
            startActivityForResult(intent, 200);
        });

        buttonFavoris.setOnClickListener(v -> {
            // Afficher la liste des favoris
            showFavorisDialog();
        });

        buttonConversations.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ConversationsActivity.class);
            startActivityForResult(intent, 300);
        });

        buttonDeconnexion.setOnClickListener(v -> {
            montrerDialogueDeconnexion();
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

        String[] titresFavoris = new String[favoris.size()];
        for (int i = 0; i < favoris.size(); i++) {
            Annonce a = favoris.get(i);
            titresFavoris[i] = a.getTitre() + " - " + a.getPrix() + "€";
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Mes favoris")
                .setItems(titresFavoris, (dialog, which) -> {
                    Annonce annonce = favoris.get(which);
                    Intent intent = new Intent(getActivity(), com.souketudiant.DetailAnnonceActivity.class);
                    intent.putExtra("annonce_id", annonce.getId());
                    startActivity(intent);
                })
                .setPositiveButton("Fermer", null)
                .show();
    }

    private void montrerDialogueDeconnexion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Déconnexion");
        builder.setMessage("Voulez-vous vraiment vous déconnecter ?");

        builder.setPositiveButton("Oui", (dialog, which) -> {
            deconnecterUtilisateur();
        });

        builder.setNegativeButton("Non", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }

    private void deconnecterUtilisateur() {
        Toast.makeText(getContext(), "Déconnexion...", Toast.LENGTH_SHORT).show();

        if (utilisateurCourant != null) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    utilisateurCourant.setEstConnecte(false);
                }
            });
        }

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK) {
            // Retour de MesAnnoncesActivity — rafraîchir les stats
            chargerStatistiques();
        } else if (requestCode == 300 && resultCode == RESULT_OK) {
            // Retour de ConversationsActivity
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mesAnnonces != null) {
            mesAnnonces.removeAllChangeListeners();
        }
        if (mesFavoris != null) {
            mesFavoris.removeAllChangeListeners();
        }
        if (realm != null) {
            realm.close();
        }
    }
}