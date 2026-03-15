package com.souketudiant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.souketudiant.models.Annonce;
import com.souketudiant.models.Message;
import com.souketudiant.models.Utilisateur;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class ConversationsActivity extends AppCompatActivity {

    private Realm realm;
    private RecyclerView recyclerView;
    private ConversationsAdapter adapter;
    private Utilisateur utilisateurConnecte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        // Configuration toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mes conversations");
        }

        realm = Realm.getDefaultInstance();
        utilisateurConnecte = realm.where(Utilisateur.class).equalTo("estConnecte", true).findFirst();

        recyclerView = findViewById(R.id.recyclerViewConversations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        chargerConversations();
    }

    private void chargerConversations() {
        RealmResults<Message> messages = realm.where(Message.class)
                .beginGroup()
                .equalTo("expediteur.id", utilisateurConnecte.getId())
                .or()
                .equalTo("destinataire.id", utilisateurConnecte.getId())
                .endGroup()
                .findAllAsync();

        messages.addChangeListener(collection -> {
            List<Annonce> conversations = new ArrayList<>();
            for (Message message : collection) {
                Annonce annonce = message.getAnnonce();
                if (!conversations.contains(annonce)) {
                    conversations.add(annonce);
                }
            }
            adapter = new ConversationsAdapter(conversations);
            recyclerView.setAdapter(adapter);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_OK);
        finish();
        return true;
    }

    private class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ViewHolder> {
        private List<Annonce> conversations;

        ConversationsAdapter(List<Annonce> conversations) {
            this.conversations = conversations;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_conversation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Annonce annonce = conversations.get(position);

            holder.titre.setText(annonce.getTitre());

            Message dernierMessage = realm.where(Message.class)
                    .equalTo("annonce.id", annonce.getId())
                    .sort("dateEnvoi", io.realm.Sort.DESCENDING)
                    .findFirst();

            if (dernierMessage != null) {
                String prefix = dernierMessage.getExpediteur().getId().equals(utilisateurConnecte.getId()) ?
                        "Vous: " : (dernierMessage.getExpediteur().getNom() + ": ");
                holder.dernierMessage.setText(prefix + dernierMessage.getContenu());

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.FRANCE);
                holder.date.setText(sdf.format(dernierMessage.getDateEnvoi()));
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ConversationsActivity.this, ConversationActivity.class);
                intent.putExtra("annonce_id", annonce.getId());
                intent.putExtra("vendeur_id", annonce.getVendeur().getId());
                intent.putExtra("acheteur_id", utilisateurConnecte.getId());
                startActivityForResult(intent, 500);
            });
        }

        @Override
        public int getItemCount() {
            return conversations.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView titre, dernierMessage, date;

            ViewHolder(View itemView) {
                super(itemView);
                titre = itemView.findViewById(R.id.textViewTitre);
                dernierMessage = itemView.findViewById(R.id.textViewDernierMessage);
                date = itemView.findViewById(R.id.textViewDate);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500 && resultCode == RESULT_OK) {
            // Retour de ConversationActivity — pas besoin de rafraîchir
            // le changeListener Realm s'en charge automatiquement
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}