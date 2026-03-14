package com.souketudiant.utils;

import com.souketudiant.models.Annonce;
import com.souketudiant.models.Utilisateur;

import io.realm.Realm;

public class DonneesTest {

    public static void genererDonneesTest(Realm realm) {
        // Vérifier si on a déjà des données
        if (realm.where(Annonce.class).count() > 0) {
            return; // On a déjà des données, on ne fait rien
        }

        // Générer des IDs
        String userId = java.util.UUID.randomUUID().toString();
        String annonceId1 = java.util.UUID.randomUUID().toString();
        String annonceId2 = java.util.UUID.randomUUID().toString();

        realm.executeTransaction(r -> {
            // Créer un utilisateur test
            Utilisateur utilisateur = r.createObject(Utilisateur.class, userId);
            utilisateur.setNom("Étudiant Test");
            utilisateur.setEmail("test@etudiant.univ.fr");
            utilisateur.setTelephone("0123456789");
            utilisateur.setFiliere("Informatique");

            // Créer 2 annonces test
            Annonce annonce1 = r.createObject(Annonce.class, annonceId1);
            annonce1.setTitre("Livre de maths L1");
            annonce1.setDescription("Livre en très bon état, comme neuf");
            annonce1.setPrix(15.99);
            annonce1.setCategorie("Livres");
            annonce1.setEtat("Bon état");
            annonce1.setVendeur(utilisateur);

            Annonce annonce2 = r.createObject(Annonce.class, annonceId2);
            annonce2.setTitre("Calculatrice scientifique");
            annonce2.setDescription("Calculatrice Texas Instruments, parfaite pour les exams");
            annonce2.setPrix(25.00);
            annonce2.setCategorie("Calculatrices");
            annonce2.setEtat("Bon état");
            annonce2.setVendeur(utilisateur);
        });
    }
}
