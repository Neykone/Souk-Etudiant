package com.souketudiant.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Base64;

import com.souketudiant.models.Annonce;
import com.souketudiant.models.Utilisateur;

import java.io.ByteArrayOutputStream;

import io.realm.Realm;

public class DonneesTest {

    public static void genererDonneesTest(Realm realm) {
        if (realm.where(Annonce.class).count() > 0) {
            return;
        }

        String userId = java.util.UUID.randomUUID().toString();
        String annonceId1 = java.util.UUID.randomUUID().toString();
        String annonceId2 = java.util.UUID.randomUUID().toString();

        realm.executeTransaction(r -> {
            Utilisateur utilisateur = r.createObject(Utilisateur.class, userId);
            utilisateur.setNom("Étudiant Test");
            utilisateur.setEmail("test@etudiant.univ.fr");
            utilisateur.setTelephone("0123456789");
            utilisateur.setFiliere("Informatique");

            Annonce annonce1 = r.createObject(Annonce.class, annonceId1);
            annonce1.setTitre("Livre de maths L1");
            annonce1.setDescription("Livre en très bon état, comme neuf. Idéal pour réviser les examens.");
            annonce1.setPrix(15.99);
            annonce1.setCategorie("Livres");
            annonce1.setEtat("Bon état");
            annonce1.setVendeur(utilisateur);
            annonce1.setEstFavori(false);  // Initialisation
            annonce1.setNombreFavoris(0);   // Initialisation

            Annonce annonce2 = r.createObject(Annonce.class, annonceId2);
            annonce2.setTitre("Calculatrice scientifique TI-82");
            annonce2.setDescription("Calculatrice Texas Instruments, parfaite pour les exams de maths et physique.");
            annonce2.setPrix(25.00);
            annonce2.setCategorie("Calculatrices");
            annonce2.setEtat("Bon état");
            annonce2.setVendeur(utilisateur);
            annonce2.setEstFavori(false);   // Initialisation
            annonce2.setNombreFavoris(0);    // Initialisation
        });
    }

    private static String genererImageFactice() {
        // Pour la démo, on retourne une chaîne vide (image par défaut)
        // Dans une vraie app, vous prendriez une photo depuis la galerie
        return "";
    }
}