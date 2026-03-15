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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mes annonces");
        }

        realm = Realm.getDefaultInstance();
        utilisateurCourant = realm.where(Utilisateur.class).equalTo("estConnecte", true).findFirst();

        initViews();
        setupRecyclerView();
        chargerMesAnnonces();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewMesAnnonces);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        buttonPublier = findViewById(R.id.buttonPublier);

        buttonPublier.setOnClickListener(v -> {
            // Fermer cette activity pour revenir à MainActivity,
            // puis MainActivity sélectionne l'onglet Publier
            finish();
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new AnnonceAdapter(
                java.util.Collections.emptyList(),
                annonce -> {
                    showOptionsDialog(annonce);
                },
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
                            modifierAnnonce(annonce);
                            break;
                        case 1:
                            marquerCommeVendue(annonce);
                            break;
                        case 2:
                            supprimerAnnonce(annonce);
                            break;
                        case 3:
                            voirDetails(annonce);
                            break;
                    }
                })
                .show();
    }

    private static final int CODE_MODIFIER = 400;

    private void modifierAnnonce(Annonce annonce) {
        Intent intent = new Intent(this, ModifierAnnonceActivity.class);
        intent.putExtra("annonce_id", annonce.getId());
        startActivityForResult(intent, CODE_MODIFIER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_MODIFIER && resultCode == RESULT_OK) {
            // Rafraîchir la liste après modification
            adapter.notifyDataSetChanged();
        }
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
        setResult(RESULT_OK);
        finish();
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