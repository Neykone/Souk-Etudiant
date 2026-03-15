package com.souketudiant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.souketudiant.adapters.AnnonceAdapter;
import com.souketudiant.models.Annonce;
import com.souketudiant.models.Utilisateur;

import io.realm.Realm;
import io.realm.RealmResults;

public class MesAnnoncesActivity extends AppCompatActivity {

    private Realm realm;
    private RecyclerView recyclerView;
    private AnnonceAdapter adapter;
    private RealmResults<Annonce> mesAnnonces;
    private Utilisateur utilisateurCourant;
    private TextView textViewEmpty;
    private MaterialButton buttonPublier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mes_annonces);

        // Configuration de la toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Mes annonces");

        realm = Realm.getDefaultInstance();
        utilisateurCourant = realm.where(Utilisateur.class).findFirst();

        initViews();
        setupRecyclerView();
        chargerMesAnnonces();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewMesAnnonces);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        buttonPublier = findViewById(R.id.buttonPublier);

        buttonPublier.setOnClickListener(v -> {
            // Aller à l'onglet publication
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("onglet", "publier");
            startActivity(intent);
            finish();
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new AnnonceAdapter(
                java.util.Collections.emptyList(),
                // Click sur l'annonce pour modifier
                annonce -> {
                    showOptionsDialog(annonce);
                },
                // Click sur favori (désactivé dans cette vue)
                (annonce, position) -> {}
        );

        recyclerView.setAdapter(adapter);
    }

    private void chargerMesAnnonces() {
        if (utilisateurCourant == null) {
            textViewEmpty.setVisibility(View.VISIBLE);
            return;
        }

        mesAnnonces = realm.where(Annonce.class)
                .equalTo("vendeur.id", utilisateurCourant.getId())
                .findAllAsync();

        mesAnnonces.addChangeListener(collection -> {
            if (collection.isEmpty()) {
                textViewEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                textViewEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.updateData(realm.copyFromRealm(collection));
            }
        });
    }

    private void showOptionsDialog(Annonce annonce) {
        String[] options = {"Modifier", "Marquer comme vendue", "Supprimer", "Voir les détails"};

        new MaterialAlertDialogBuilder(this)
                .setTitle(annonce.getTitre())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Modifier
                            modifierAnnonce(annonce);
                            break;
                        case 1:
                            // Marquer comme vendue
                            marquerCommeVendue(annonce);
                            break;
                        case 2:
                            // Supprimer
                            supprimerAnnonce(annonce);
                            break;
                        case 3:
                            // Voir détails
                            voirDetails(annonce);
                            break;
                    }
                })
                .show();
    }

    private void modifierAnnonce(Annonce annonce) {
        Intent intent = new Intent(this, ModifierAnnonceActivity.class);
        intent.putExtra("annonce_id", annonce.getId());
        startActivity(intent);
    }

    private void marquerCommeVendue(Annonce annonce) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmation")
                .setMessage("Marquer cette annonce comme vendue ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    realm.executeTransactionAsync(r -> {
                        Annonce a = r.where(Annonce.class)
                                .equalTo("id", annonce.getId())
                                .findFirst();
                        if (a != null) {
                            a.setEstVendu(true);
                        }
                    }, () -> {
                        Toast.makeText(this, "Annonce marquée comme vendue", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void supprimerAnnonce(Annonce annonce) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmation")
                .setMessage("Voulez-vous vraiment supprimer cette annonce ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    realm.executeTransactionAsync(r -> {
                        Annonce a = r.where(Annonce.class)
                                .equalTo("id", annonce.getId())
                                .findFirst();
                        if (a != null) {
                            a.deleteFromRealm();
                        }
                    }, () -> {
                        Toast.makeText(this, "Annonce supprimée", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void voirDetails(Annonce annonce) {
        Intent intent = new Intent(this, DetailAnnonceActivity.class);
        intent.putExtra("annonce_id", annonce.getId());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mesAnnonces != null) {
            mesAnnonces.removeAllChangeListeners();
        }
        if (realm != null) {
            realm.close();
        }
    }
}
