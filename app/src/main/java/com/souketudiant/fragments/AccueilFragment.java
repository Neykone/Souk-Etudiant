package com.souketudiant.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.souketudiant.DetailAnnonceActivity;
import com.souketudiant.R;
import com.souketudiant.adapters.AnnonceAdapter;
import com.souketudiant.models.Annonce;
import com.souketudiant.utils.Categories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Case;

public class AccueilFragment extends Fragment {

    private Realm realm;
    private RecyclerView recyclerView;

    private AnnonceAdapter adapter;
    private RealmResults<Annonce> annonces;
    private TextView textViewFiltreActif;

    // Launcher pour DetailAnnonceActivity
    private ActivityResultLauncher<Intent> detailLauncher;

    // Variables pour les filtres
    private String rechercheText = "";
    private String categorieFilter = "Tous";
    private double prixMinFilter = 0;
    private double prixMaxFilter = 1000;
    private String etatFilter = "Tous";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accueil, container, false);

        // Enregistrer le launcher — doit être fait avant tout startActivity
        detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Retour de DetailAnnonceActivity — rafraîchir la liste
                    chargerAnnonces();
                }
        );

        realm = Realm.getDefaultInstance();
        textViewFiltreActif = view.findViewById(R.id.textViewFiltreActif);

        setupRecyclerView(view);
        setupMenu();
        chargerAnnonces();

        return view;
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewAnnonces);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new AnnonceAdapter(
                Collections.emptyList(),
                annonce -> {
                    Intent intent = new Intent(getActivity(), DetailAnnonceActivity.class);
                    intent.putExtra("annonce_id", annonce.getId());
                    detailLauncher.launch(intent);
                },
                (annonce, position) -> {
                    // Gestion des favoris
                    toggleFavori(annonce, position);
                }
        );

        recyclerView.setAdapter(adapter);
    }

    private void setupMenu() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_accueil, menu);

                MenuItem searchItem = menu.findItem(R.id.action_search);
                SearchView searchView = (SearchView) searchItem.getActionView();
                searchView.setQueryHint("Rechercher un article...");

                // AJOUTEZ CES 2 LIGNES
                searchView.setIconifiedByDefault(false);
                searchView.setMaxWidth(Integer.MAX_VALUE);

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        rechercheText = query != null ? query : "";
                        chargerAnnonces();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        rechercheText = newText != null ? newText : "";
                        chargerAnnonces();
                        return true;
                    }
                });

                searchView.setOnCloseListener(() -> {
                    rechercheText = "";
                    chargerAnnonces();
                    return false;
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_filter) {
                    showFilterDialog();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }



    private void toggleFavori(Annonce annonce, int position) {
        String annonceId = annonce.getId();

        realm.executeTransactionAsync(r -> {
            Annonce annonceToUpdate = r.where(Annonce.class)
                    .equalTo("id", annonceId)
                    .findFirst();
            if (annonceToUpdate != null) {
                boolean nouveauStatut = !annonceToUpdate.isEstFavori();
                annonceToUpdate.setEstFavori(nouveauStatut);

                int nouveauNombre = annonceToUpdate.getNombreFavoris() + (nouveauStatut ? 1 : -1);
                annonceToUpdate.setNombreFavoris(Math.max(0, nouveauNombre));
            }
        }, () -> {
            String message = annonce.isEstFavori() ?
                    "❌ Retiré aux favoris" :
                    "✅ Ajouté des favoris";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            chargerAnnonces();
        });
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filtres, null);

        Spinner spinnerCategorie = dialogView.findViewById(R.id.spinnerCategorie);
        EditText editTextPrixMin = dialogView.findViewById(R.id.editTextPrixMin);
        EditText editTextPrixMax = dialogView.findViewById(R.id.editTextPrixMax);
        RadioGroup radioGroupEtat = dialogView.findViewById(R.id.radioGroupEtat);

        // Configurer le spinner des catégories
        List<String> categories = new ArrayList<>();
        categories.add("Tous");
        Collections.addAll(categories, Categories.CATEGORIES);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategorie.setAdapter(adapter);

        // Pré-remplir avec les valeurs actuelles
        int categoriePosition = categories.indexOf(categorieFilter);
        if (categoriePosition >= 0) {
            spinnerCategorie.setSelection(categoriePosition);
        }

        editTextPrixMin.setText(String.valueOf(prixMinFilter));
        editTextPrixMax.setText(String.valueOf(prixMaxFilter));

        switch (etatFilter) {
            case "Neuf":
                radioGroupEtat.check(R.id.radioNeuf);
                break;
            case "Bon état":
                radioGroupEtat.check(R.id.radioBonEtat);
                break;
            case "Usagé":
                radioGroupEtat.check(R.id.radioUsage);
                break;
            default:
                radioGroupEtat.check(R.id.radioTous);
                break;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Filtres")
                .setView(dialogView)
                .setPositiveButton("Appliquer", (dialog, which) -> {
                    categorieFilter = spinnerCategorie.getSelectedItem().toString();

                    try {
                        prixMinFilter = Double.parseDouble(editTextPrixMin.getText().toString());
                    } catch (NumberFormatException e) {
                        prixMinFilter = 0;
                    }

                    try {
                        prixMaxFilter = Double.parseDouble(editTextPrixMax.getText().toString());
                    } catch (NumberFormatException e) {
                        prixMaxFilter = 1000;
                    }

                    int selectedEtatId = radioGroupEtat.getCheckedRadioButtonId();
                    if (selectedEtatId == R.id.radioNeuf) {
                        etatFilter = "Neuf";
                    } else if (selectedEtatId == R.id.radioBonEtat) {
                        etatFilter = "Bon état";
                    } else if (selectedEtatId == R.id.radioUsage) {
                        etatFilter = "Usagé";
                    } else {
                        etatFilter = "Tous";
                    }

                    updateFilterDisplay();
                    chargerAnnonces();
                })
                .setNeutralButton("Réinitialiser", (dialog, which) -> {
                    resetFilters();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void resetFilters() {
        rechercheText = "";
        categorieFilter = "Tous";
        prixMinFilter = 0;
        prixMaxFilter = 1000;
        etatFilter = "Tous";
        updateFilterDisplay();
        chargerAnnonces();
    }

    private void updateFilterDisplay() {
        StringBuilder filterText = new StringBuilder();

        if (!rechercheText.isEmpty()) {
            filterText.append("Recherche: ").append(rechercheText).append(" | ");
        }

        if (!categorieFilter.equals("Tous")) {
            filterText.append("Catégorie: ").append(categorieFilter).append(" | ");
        }

        if (prixMinFilter > 0 || prixMaxFilter < 1000) {
            filterText.append("Prix: ").append(prixMinFilter).append("€ - ").append(prixMaxFilter).append("€ | ");
        }

        if (!etatFilter.equals("Tous")) {
            filterText.append("État: ").append(etatFilter);
        }

        if (filterText.length() > 0) {
            textViewFiltreActif.setText("Filtres actifs: " + filterText.toString());
            textViewFiltreActif.setVisibility(View.VISIBLE);
        } else {
            textViewFiltreActif.setVisibility(View.GONE);
        }
    }

    private void chargerAnnonces() {
        if (realm == null || realm.isClosed()) return;

        RealmQuery<Annonce> query = realm.where(Annonce.class);

        // Filtre par recherche texte (MODIFIÉ)
        if (rechercheText != null && !rechercheText.isEmpty()) {
            query = query.beginGroup()
                    .contains("titre", rechercheText, io.realm.Case.INSENSITIVE)
                    .or()
                    .contains("description", rechercheText, io.realm.Case.INSENSITIVE)
                    .endGroup();
        }

        // Filtre par catégorie
        if (!categorieFilter.equals("Tous")) {
            query = query.equalTo("categorie", categorieFilter);
        }

        // Filtre par prix
        query = query.between("prix", prixMinFilter, prixMaxFilter);

        // Filtre par état
        if (!etatFilter.equals("Tous")) {
            query = query.equalTo("etat", etatFilter);
        }

        // Ne montrer que les annonces non vendues
        query = query.equalTo("estVendu", false);

        annonces = query.findAllAsync();

        annonces.addChangeListener(collection -> {
            adapter.updateData(realm.copyFromRealm(collection));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (annonces != null) {
            annonces.removeAllChangeListeners();
        }
        if (realm != null) {
            realm.close();
        }
    }
}