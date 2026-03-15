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

    private List<Message> messages;
    private String currentUserId;

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getExpediteur() != null &&
                message.getExpediteur().getId().equals(currentUserId)) {
            return TYPE_MESSAGE_ENVOYE;
        } else {
            return TYPE_MESSAGE_RECU;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_MESSAGE_ENVOYE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_envoye, parent, false);
            return new MessageEnvoyeViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_recu, parent, false);
            return new MessageRecuViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            Message message = messages.get(position);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.FRANCE);

            if (holder instanceof MessageEnvoyeViewHolder) {
                MessageEnvoyeViewHolder envoyeHolder = (MessageEnvoyeViewHolder) holder;
                envoyeHolder.textViewContenu.setText(message.getContenu() != null ? message.getContenu() : "");
                envoyeHolder.textViewHeure.setText(sdf.format(message.getDateEnvoi()));

                if (message.isEstLu()) {
                    envoyeHolder.textViewStatut.setText("Lu");
                    envoyeHolder.textViewStatut.setVisibility(View.VISIBLE);
                } else {
                    envoyeHolder.textViewStatut.setText("Envoyé");
                    envoyeHolder.textViewStatut.setVisibility(View.VISIBLE);
                }

            } else if (holder instanceof MessageRecuViewHolder) {
                MessageRecuViewHolder recuHolder = (MessageRecuViewHolder) holder;
                recuHolder.textViewContenu.setText(message.getContenu() != null ? message.getContenu() : "");
                recuHolder.textViewHeure.setText(sdf.format(message.getDateEnvoi()));

                if (message.getExpediteur() != null) {
                    recuHolder.textViewExpediteur.setText(message.getExpediteur().getNom());
                } else {
                    recuHolder.textViewExpediteur.setText("Inconnu");
                }
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

    static class MessageEnvoyeViewHolder extends RecyclerView.ViewHolder {
        TextView textViewContenu;
        TextView textViewHeure;
        TextView textViewStatut;

        MessageEnvoyeViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewContenu = itemView.findViewById(R.id.textViewContenu);
            textViewHeure = itemView.findViewById(R.id.textViewHeure);
            textViewStatut = itemView.findViewById(R.id.textViewStatut);
        }
    }

    static class MessageRecuViewHolder extends RecyclerView.ViewHolder {
        TextView textViewContenu;
        TextView textViewHeure;
        TextView textViewExpediteur;

        MessageRecuViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewContenu = itemView.findViewById(R.id.textViewContenu);
            textViewHeure = itemView.findViewById(R.id.textViewHeure);
            textViewExpediteur = itemView.findViewById(R.id.textViewExpediteur);
        }
    }
}