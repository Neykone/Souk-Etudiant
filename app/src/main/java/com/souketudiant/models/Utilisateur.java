package com.souketudiant.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;
import java.util.Date;

public class Utilisateur extends RealmObject {

    @PrimaryKey
    private String id;

    @Required
    private String nom;

    @Required
    private String email;

    private String telephone;
    private String filiere;
    private Date dateInscription;
    private String photoUrl;

    // Constructeurs
    public Utilisateur() {
        this.id = java.util.UUID.randomUUID().toString();
        this.dateInscription = new Date();
    }

    public Utilisateur(String nom, String email) {
        this.id = java.util.UUID.randomUUID().toString();
        this.nom = nom;
        this.email = email;
        this.dateInscription = new Date();
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }

    public Date getDateInscription() { return dateInscription; }
    public void setDateInscription(Date dateInscription) { this.dateInscription = dateInscription; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
