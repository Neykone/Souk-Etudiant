package com.souketudiant.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.souketudiant.R;
import com.souketudiant.models.Annonce;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;

public class AnnonceAdapter extends RecyclerView.Adapter<AnnonceAdapter.AnnonceViewHolder> {

    private List<Annonce> annonces;
    private OnItemClickListener listener;
    private OnFavoriClickListener favoriListener;

    public interface OnItemClickListener {
        void onItemClick(Annonce annonce);
    }

    public interface OnFavoriClickListener {
        void onFavoriClick(Annonce annonce, int position);
    }

    public AnnonceAdapter(List<Annonce> annonces, OnItemClickListener listener, OnFavoriClickListener favoriListener) {
        this.annonces = annonces;
        this.listener = listener;
        this.favoriListener = favoriListener;
    }

    @NonNull
    @Override
    public AnnonceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_annonce, parent, false);
        return new AnnonceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnonceViewHolder holder, int position) {
        Annonce annonce = annonces.get(position);
        holder.bind(annonce, listener, favoriListener, position);
    }

    @Override
    public int getItemCount() {
        return annonces.size();
    }

    public void updateData(List<Annonce> nouvellesAnnonces) {
        this.annonces = nouvellesAnnonces;
        notifyDataSetChanged();
    }

    static class AnnonceViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView textViewTitre;
        private TextView textViewPrix;
        private TextView textViewDescription;
        private TextView textViewCategorie;
        private TextView textViewEtat;
        private TextView textViewVendeur;
        private ImageView imageViewFavori;
        private Realm realm;

        public AnnonceViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewAnnonce);
            textViewTitre = itemView.findViewById(R.id.textViewTitre);
            textViewPrix = itemView.findViewById(R.id.textViewPrix);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewCategorie = itemView.findViewById(R.id.textViewCategorie);
            textViewEtat = itemView.findViewById(R.id.textViewEtat);
            textViewVendeur = itemView.findViewById(R.id.textViewVendeur);
            imageViewFavori = itemView.findViewById(R.id.imageViewFavori);
            realm = Realm.getDefaultInstance();
        }

        public void bind(final Annonce annonce, final OnItemClickListener itemListener,
                         final OnFavoriClickListener favoriListener, final int position) {
            // Titre
            textViewTitre.setText(annonce.getTitre());

            // Prix formaté
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.FRANCE);
            String prixFormate = format.format(annonce.getPrix());
            textViewPrix.setText(prixFormate);

            // Description
            if (annonce.getDescription() != null && !annonce.getDescription().isEmpty()) {
                textViewDescription.setText(annonce.getDescription());
            } else {
                textViewDescription.setText("Aucune description fournie");
            }

            // Catégorie
            textViewCategorie.setText(annonce.getCategorie());

            // État
            textViewEtat.setText(annonce.getEtat());

            // Vendeur
            if (annonce.getVendeur() != null) {
                textViewVendeur.setText("Vendeur: " + annonce.getVendeur().getNom());
            }

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

            // Gestion de l'icône favori
            updateFavoriIcon(annonce.isEstFavori());

            // Clic sur l'icône favori
            imageViewFavori.setOnClickListener(v -> {
                if (favoriListener != null) {
                    favoriListener.onFavoriClick(annonce, position);
                }
            });

            // Clic sur l'item
            itemView.setOnClickListener(v -> {
                if (itemListener != null) {
                    itemListener.onItemClick(annonce);
                }
            });
        }

        private void updateFavoriIcon(boolean estFavori) {
            if (estFavori) {
                imageViewFavori.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                imageViewFavori.setImageResource(android.R.drawable.btn_star_big_off);
            }
        }
    }
}