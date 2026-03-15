package com.souketudiant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.souketudiant.R;
import com.souketudiant.models.Message;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_MESSAGE_ENVOYE = 0;
    private static final int TYPE_MESSAGE_RECU = 1;
    private static final int TYPE_LOCATION_ENVOYE = 2;
    private static final int TYPE_LOCATION_RECU = 3;

    private List<Message> messages;
    private String currentUserId;

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);

        // Déterminer si c'est un message envoyé par l'utilisateur courant
        boolean isEnvoye = message.getExpediteur() != null &&
                message.getExpediteur().getId().equals(currentUserId);

        // Vérifier le type de message
        String type = message.getType();
        if (type == null) type = "text"; // Par défaut

        if ("location".equals(type)) {
            return isEnvoye ? TYPE_LOCATION_ENVOYE : TYPE_LOCATION_RECU;
        } else {
            return isEnvoye ? TYPE_MESSAGE_ENVOYE : TYPE_MESSAGE_RECU;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case TYPE_LOCATION_ENVOYE:
                return new LocationEnvoyeViewHolder(
                        inflater.inflate(R.layout.item_message_location_envoye, parent, false));
            case TYPE_LOCATION_RECU:
                return new LocationRecuViewHolder(
                        inflater.inflate(R.layout.item_message_location_recu, parent, false));
            case TYPE_MESSAGE_ENVOYE:
                return new MessageEnvoyeViewHolder(
                        inflater.inflate(R.layout.item_message_envoye, parent, false));
            case TYPE_MESSAGE_RECU:
            default:
                return new MessageRecuViewHolder(
                        inflater.inflate(R.layout.item_message_recu, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            Message message = messages.get(position);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.FRANCE);

            if (holder instanceof MessageEnvoyeViewHolder) {
                // Message texte envoyé
                MessageEnvoyeViewHolder envoyeHolder = (MessageEnvoyeViewHolder) holder;
                envoyeHolder.textViewContenu.setText(message.getContenu() != null ? message.getContenu() : "");
                envoyeHolder.textViewHeure.setText(sdf.format(message.getDateEnvoi()));
                envoyeHolder.textViewStatut.setText(message.isEstLu() ? "Lu" : "Envoyé");
                envoyeHolder.textViewStatut.setVisibility(View.VISIBLE);

            } else if (holder instanceof MessageRecuViewHolder) {
                // Message texte reçu
                MessageRecuViewHolder recuHolder = (MessageRecuViewHolder) holder;
                recuHolder.textViewContenu.setText(message.getContenu() != null ? message.getContenu() : "");
                recuHolder.textViewHeure.setText(sdf.format(message.getDateEnvoi()));
                if (message.getExpediteur() != null) {
                    recuHolder.textViewExpediteur.setText(message.getExpediteur().getNom());
                }

            } else if (holder instanceof LocationEnvoyeViewHolder) {
                // Localisation envoyée
                LocationEnvoyeViewHolder locationHolder = (LocationEnvoyeViewHolder) holder;
                locationHolder.textViewLieuNom.setText(
                        message.getLieuNom() != null ? message.getLieuNom() : "Position partagée");
                locationHolder.textViewHeure.setText(sdf.format(message.getDateEnvoi()));
                locationHolder.textViewStatut.setText(message.isEstLu() ? "Lu" : "Envoyé");
                locationHolder.textViewStatut.setVisibility(View.VISIBLE);

                // Ajouter un click listener pour ouvrir la carte
                locationHolder.itemView.setOnClickListener(v -> {
                    // TODO: Ouvrir une carte avec la position
                    android.widget.Toast.makeText(v.getContext(),
                            "Lat: " + message.getLatitude() + ", Lng: " + message.getLongitude(),
                            android.widget.Toast.LENGTH_SHORT).show();
                });

            } else if (holder instanceof LocationRecuViewHolder) {
                // Localisation reçue
                LocationRecuViewHolder locationHolder = (LocationRecuViewHolder) holder;
                locationHolder.textViewLieuNom.setText(
                        message.getLieuNom() != null ? message.getLieuNom() : "Position partagée");
                locationHolder.textViewHeure.setText(sdf.format(message.getDateEnvoi()));
                if (message.getExpediteur() != null) {
                    locationHolder.textViewExpediteur.setText(message.getExpediteur().getNom());
                }

                // Ajouter un click listener pour ouvrir la carte
                locationHolder.itemView.setOnClickListener(v -> {
                    // TODO: Ouvrir une carte avec la position
                    android.widget.Toast.makeText(v.getContext(),
                            "Lat: " + message.getLatitude() + ", Lng: " + message.getLongitude(),
                            android.widget.Toast.LENGTH_SHORT).show();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateData(List<Message> nouveauxMessages) {
        this.messages = nouveauxMessages;
        notifyDataSetChanged();
    }

    // ViewHolders pour les messages texte
    static class MessageEnvoyeViewHolder extends RecyclerView.ViewHolder {
        TextView textViewContenu, textViewHeure, textViewStatut;
        MessageEnvoyeViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewContenu = itemView.findViewById(R.id.textViewContenu);
            textViewHeure = itemView.findViewById(R.id.textViewHeure);
            textViewStatut = itemView.findViewById(R.id.textViewStatut);
        }
    }

    static class MessageRecuViewHolder extends RecyclerView.ViewHolder {
        TextView textViewContenu, textViewHeure, textViewExpediteur;
        MessageRecuViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewContenu = itemView.findViewById(R.id.textViewContenu);
            textViewHeure = itemView.findViewById(R.id.textViewHeure);
            textViewExpediteur = itemView.findViewById(R.id.textViewExpediteur);
        }
    }

    // ViewHolders pour les messages de localisation
    static class LocationEnvoyeViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLieuNom, textViewHeure, textViewStatut;
        LocationEnvoyeViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewLieuNom = itemView.findViewById(R.id.textViewLieuNom);
            textViewHeure = itemView.findViewById(R.id.textViewHeure);
            textViewStatut = itemView.findViewById(R.id.textViewStatut);
        }
    }

    static class LocationRecuViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLieuNom, textViewHeure, textViewExpediteur;
        LocationRecuViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewLieuNom = itemView.findViewById(R.id.textViewLieuNom);
            textViewHeure = itemView.findViewById(R.id.textViewHeure);
            textViewExpediteur = itemView.findViewById(R.id.textViewExpediteur);
        }
    }
}