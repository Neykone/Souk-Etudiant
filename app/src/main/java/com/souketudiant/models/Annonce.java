package com.souketudiant.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;
import java.util.Date;

public class Annonce extends RealmObject {

    @PrimaryKey
    private String id;

    @Required
    private String titre;

    private String description;
    private double prix;
    private String categorie;
    private String etat; // "Neuf", "Bon état", "Usagé"

    private Date datePublication;
    private Date dateModification;

    // Relation avec l'utilisateur
    private Utilisateur vendeur;

    private boolean estVendu;
    private String photoUrl;

    // Constructeurs
    public Annonce() {
        this.id = java.util.UUID.randomUUID().toString();
        this.datePublication = new Date();
        this.dateModification = new Date();
        this.estVendu = false;
    }

    public Annonce(String titre, double prix, String categorie, Utilisateur vendeur) {
        this.id = java.util.UUID.randomUUID().toString();
        this.titre = titre;
        this.prix = prix;
        this.categorie = categorie;
        this.vendeur = vendeur;
        this.datePublication = new Date();
        this.dateModification = new Date();
        this.estVendu = false;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public Date getDatePublication() { return datePublication; }
    public void setDatePublication(Date datePublication) { this.datePublication = datePublication; }

    public Date getDateModification() { return dateModification; }
    public void setDateModification(Date dateModification) { this.dateModification = dateModification; }

    public Utilisateur getVendeur() { return vendeur; }
    public void setVendeur(Utilisateur vendeur) { this.vendeur = vendeur; }

    public boolean isEstVendu() { return estVendu; }
    public void setEstVendu(boolean estVendu) {
        this.estVendu = estVendu;
        this.dateModification = new Date();
    }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
