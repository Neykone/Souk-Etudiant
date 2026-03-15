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
import com.souketudiant.LoginActivity;
import com.souketudiant.MainActivity;
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
    private MaterialButton buttonDeconnexion;

    private Utilisateur utilisateurCourant;
    private RealmResults<Annonce> mesAnnonces;
    private RealmResults<Annonce> mesFavoris;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        realm = Realm.getDefaultInstance();
        utilisateurCourant = realm.where(Utilisateur.class).findFirst();

        initViews(view);
        chargerProfil();
        chargerStatistiques();
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
        buttonDeconnexion = view.findViewById(R.id.buttonDeconnexion);
    }

    private void chargerProfil() {
        if (utilisateurCourant != null) {
            textViewNom.setText(utilisateurCourant.getNom());
            textViewEmail.setText(utilisateurCourant.getEmail());
            textViewFiliere.setText(utilisateurCourant.getFiliere());
        }
    }

    private void chargerStatistiques() {
        if (utilisateurCourant == null) return;

        // ICI on filtre par utilisateur car ce sont SES annonces
        mesAnnonces = realm.where(Annonce.class)
                .equalTo("vendeur.id", utilisateurCourant.getId())
                .findAllAsync();

        mesAnnonces.addChangeListener(collection -> {
            textViewNbAnnonces.setText(String.valueOf(collection.size()));

            long vendues = collection.where().equalTo("estVendu", true).count();
            textViewNbVendues.setText(String.valueOf(vendues));
        });

        // Favoris : toutes les annonces favorites de l'utilisateur
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
            startActivity(intent);
        });

        buttonFavoris.setOnClickListener(v -> {
            showFavorisDialog();
        });

        buttonDeconnexion.setOnClickListener(v -> {
            showDeconnexionDialog();
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
                    // Ouvrir le détail
                    Annonce annonce = favoris.get(which);
                    // TODO: Ouvrir DetailAnnonceActivity
                    Toast.makeText(getContext(),
                            "Ouverture de " + annonce.getTitre(),
                            Toast.LENGTH_SHORT).show();
                })
                .setPositiveButton("Fermer", null)
                .show();
    }

    private void showDeconnexionDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Déconnexion")
                .setMessage("Voulez-vous vraiment vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    // D'abord, fermer la dialogue
                    dialog.dismiss();

                    // Récupérer l'ID de l'utilisateur avant la transaction
                    String userId = utilisateurCourant != null ? utilisateurCourant.getId() : null;

                    if (userId == null) {
                        // Pas d'utilisateur, rediriger directement
                        redirigerVersLogin();
                        return;
                    }

                    // Mettre à jour le statut de connexion dans Realm
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Utilisateur user = realm.where(Utilisateur.class)
                                    .equalTo("id", userId)
                                    .findFirst();
                            if (user != null) {
                                user.setEstConnecte(false);
                            }
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            // Transaction réussie
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Déconnexion réussie", Toast.LENGTH_SHORT).show();
                            }
                            redirigerVersLogin();
                        }
                    }, new Realm.Transaction.OnError() {
                        @Override
                        public void onError(Throwable error) {
                            // Erreur mais on redirige quand même
                            error.printStackTrace();
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Déconnexion effectuée", Toast.LENGTH_SHORT).show();
                            }
                            redirigerVersLogin();
                        }
                    });
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void redirigerVersLogin() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
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