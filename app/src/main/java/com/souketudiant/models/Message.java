package com.souketudiant.models;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Message extends RealmObject {

    @PrimaryKey
    private String id;

    @Required
    private String contenu;

    private Date dateEnvoi;
    private boolean estLu;

    // Relation avec l'annonce concernée
    private Annonce annonce;

    // Expéditeur et destinataire
    private Utilisateur expediteur;
    private Utilisateur destinataire;

    // Constructeurs
    public Message() {
        this.id = UUID.randomUUID().toString();
        this.dateEnvoi = new Date();
        this.estLu = false;
    }

    public Message(String contenu, Annonce annonce, Utilisateur expediteur, Utilisateur destinataire) {
        this.id = UUID.randomUUID().toString();
        this.contenu = contenu;
        this.annonce = annonce;
        this.expediteur = expediteur;
        this.destinataire = destinataire;
        this.dateEnvoi = new Date();
        this.estLu = false;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public Date getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(Date dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public boolean isEstLu() { return estLu; }
    public void setEstLu(boolean estLu) { this.estLu = estLu; }

    public Annonce getAnnonce() { return annonce; }
    public void setAnnonce(Annonce annonce) { this.annonce = annonce; }

    public Utilisateur getExpediteur() { return expediteur; }
    public void setExpediteur(Utilisateur expediteur) { this.expediteur = expediteur; }

    public Utilisateur getDestinataire() { return destinataire; }
    public void setDestinataire(Utilisateur destinataire) { this.destinataire = destinataire; }
}
