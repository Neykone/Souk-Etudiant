package com.souketudiant.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;
import java.util.Date;
import io.realm.RealmList;

public class Utilisateur extends RealmObject {

    @PrimaryKey
    private String id;

    @Required
    private String nom;

    @Required
    private String email;

    @Required
    private String motDePasse;  // Ajout du champ mot de passe

    private String telephone;
    private String filiere;
    private Date dateInscription;
    private String photoUrl;
    private boolean estConnecte;  // Pour savoir si l'utilisateur est connecté
    private RealmList<String> annoncesFavorisIds;
    // Constructeurs
    public Utilisateur() {
        this.id = java.util.UUID.randomUUID().toString();
        this.dateInscription = new Date();
        this.estConnecte = false;
        this.annoncesFavorisIds = new RealmList<>();
    }

    public Utilisateur(String nom, String email, String motDePasse) {
        this.id = java.util.UUID.randomUUID().toString();
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.dateInscription = new Date();
        this.estConnecte = false;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }

    public Date getDateInscription() { return dateInscription; }
    public void setDateInscription(Date dateInscription) { this.dateInscription = dateInscription; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public boolean isEstConnecte() { return estConnecte; }
    public void setEstConnecte(boolean estConnecte) { this.estConnecte = estConnecte; }
    public RealmList<String> getAnnoncesFavorisIds() { return annoncesFavorisIds; }
    public void setAnnoncesFavorisIds(RealmList<String> annoncesFavorisIds) { this.annoncesFavorisIds = annoncesFavorisIds; }
}