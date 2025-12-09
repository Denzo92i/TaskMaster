package com.taskmaster.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Classe utilitaire pour hasher et vérifier les mots de passe
 * Utilise BCrypt (meilleur que SHA-256 pour les mots de passe)
 *
 * RESPONSABLE : Dylan
 */
public class PasswordHasher {

    // Coût du hashage (10-12 = bon compromis sécurité/performance)
    private static final int BCRYPT_ROUNDS = 10;

    /**
     * Hashe un mot de passe en clair
     *
     * Exemple :
     *   String hash = PasswordHasher.hashPassword("password123");
     *   // hash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p..."
     *
     * @param plainPassword Le mot de passe en clair
     * @return Le mot de passe hashé (peut être stocké en BDD)
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Vérifie si un mot de passe correspond au hash
     *
     * Exemple :
     *   String hash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p...";
     *   boolean correct = PasswordHasher.checkPassword("password123", hash);
     *   // correct = true
     *
     * @param plainPassword Le mot de passe entré par l'utilisateur
     * @param hashedPassword Le hash stocké en base de données
     * @return true si le mot de passe est correct
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Hash invalide
            return false;
        }
    }

    /**
     * Teste si le hashage fonctionne correctement
     * Utile pour déboguer
     */
    public static void main(String[] args) {
        System.out.println("===== TEST DE HASHAGE =====");

        String password = "password123";

        // 1. Hasher le mot de passe
        String hash = hashPassword(password);
        System.out.println("Mot de passe : " + password);
        System.out.println("Hash BCrypt  : " + hash);

        // 2. Vérifier le mot de passe correct
        boolean correct = checkPassword("password123", hash);
        System.out.println("\nTest avec bon mot de passe : " + (correct ? "✓ OK" : "✗ FAIL"));

        // 3. Vérifier un mot de passe incorrect
        boolean wrong = checkPassword("wrongpassword", hash);
        System.out.println("Test avec mauvais mot de passe : " + (!wrong ? "✓ OK" : "✗ FAIL"));

        // 4. Même mot de passe = hash différents (sel aléatoire)
        String hash2 = hashPassword(password);
        System.out.println("\nHash du même mot de passe : " + hash2);
        System.out.println("Les hash sont différents : " + (!hash.equals(hash2) ? "✓ OK (normal)" : "✗ Problème"));
        System.out.println("Mais les deux sont valides : " + (checkPassword(password, hash2) ? "✓ OK" : "✗ FAIL"));
    }
}

/*
 * ===== EXPLICATION DÉTAILLÉE =====
 *
 * 1. POURQUOI HASHER LES MOTS DE PASSE ?
 *    - Stocker "password123" en clair = DANGEREUX
 *    - Si quelqu'un vole la BDD, il voit tous les mots de passe
 *    - Avec hashage : "$2a$10$..." → Impossible de retrouver le mot de passe
 *
 * 2. POURQUOI BCRYPT ET PAS SHA-256 ?
 *    - SHA-256 = rapide → Les hackers peuvent tester des millions de mots de passe/sec
 *    - BCrypt = lent volontairement → Protège contre les attaques "brute force"
 *    - BCrypt intègre un "sel" (salt) automatiquement → Même mot de passe = hash différent
 *
 * 3. COMMENT ÇA MARCHE ?
 *    Inscription :
 *      1. L'utilisateur entre : "password123"
 *      2. On hash : "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p..."
 *      3. On stocke le HASH en BDD (pas le mot de passe)
 *
 *    Connexion :
 *      1. L'utilisateur entre : "password123"
 *      2. On récupère le hash en BDD
 *      3. BCrypt.checkpw() compare → true ou false
 *
 * 4. LE HASH EST-IL TOUJOURS LE MÊME ?
 *    NON ! Même mot de passe = hash différent à chaque fois
 *    Car BCrypt ajoute un "sel" aléatoire
 *    Mais checkPassword() fonctionne quand même !
 *
 * 5. COMMENT TESTER ?
 *    - Clic droit sur PasswordHasher.java
 *    - "Run PasswordHasher.main()"
 *    - Regardez la console
 */