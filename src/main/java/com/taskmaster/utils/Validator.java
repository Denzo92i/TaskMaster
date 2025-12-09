package com.taskmaster.utils;

import java.util.regex.Pattern;

/**
 * Classe utilitaire pour valider les entrées utilisateur
 * Empêche les données invalides (emails mal formés, mots de passe faibles, etc.)
 *
 * RESPONSABLE : Adam
 */
public class Validator {

    // Expressions régulières (regex) pour validation
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_-]{3,20}$");

    /**
     * Valide une adresse email
     *
     * Exemples valides : john@example.com, user.name@domain.co.uk
     * Exemples invalides : john@, @example.com, john@domain
     *
     * @param email L'email à valider
     * @return true si l'email est valide
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Valide un nom d'utilisateur (username)
     *
     * Règles :
     * - Entre 3 et 20 caractères
     * - Seulement lettres, chiffres, tirets (-) et underscores (_)
     * - Pas d'espaces
     *
     * @param username Le username à valider
     * @return true si valide
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /**
     * Valide la force d'un mot de passe
     *
     * Règles :
     * - Au moins 8 caractères
     * - Au moins une lettre majuscule
     * - Au moins une lettre minuscule
     * - Au moins un chiffre
     *
     * @param password Le mot de passe à valider
     * @return true si le mot de passe est fort
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }

        return hasUpper && hasLower && hasDigit;
    }

    /**
     * Vérifie si une chaîne n'est pas vide
     *
     * @param str La chaîne à vérifier
     * @return true si la chaîne contient du texte
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Valide un nom ou prénom
     *
     * Règles :
     * - Au moins 2 caractères
     * - Maximum 100 caractères
     * - Seulement lettres, espaces, tirets et apostrophes
     *
     * @param name Le nom à valider
     * @return true si valide
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        String trimmed = name.trim();
        if (trimmed.length() < 2 || trimmed.length() > 100) {
            return false;
        }

        // Accepte lettres, espaces, tirets, apostrophes (pour "Jean-Pierre" ou "O'Connor")
        return trimmed.matches("^[A-Za-zÀ-ÿ\\s'-]+$");
    }

    /**
     * Vérifie si deux mots de passe correspondent
     * Utile pour "Confirmer le mot de passe"
     *
     * @param password Premier mot de passe
     * @param confirmPassword Confirmation
     * @return true si identiques
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Obtient un message d'erreur détaillé pour un mot de passe faible
     *
     * @param password Le mot de passe à analyser
     * @return Message expliquant pourquoi le mot de passe est faible
     */
    public static String getPasswordError(String password) {
        if (password == null || password.isEmpty()) {
            return "Le mot de passe ne peut pas être vide";
        }

        if (password.length() < 8) {
            return "Le mot de passe doit contenir au moins 8 caractères";
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }

        if (!hasUpper) return "Le mot de passe doit contenir au moins une majuscule";
        if (!hasLower) return "Le mot de passe doit contenir au moins une minuscule";
        if (!hasDigit) return "Le mot de passe doit contenir au moins un chiffre";

        return ""; // Pas d'erreur
    }

    /**
     * Teste toutes les validations
     * Pour déboguer et comprendre comment ça marche
     */
    public static void main(String[] args) {
        System.out.println("===== TESTS DE VALIDATION =====\n");

        // Test emails
        System.out.println("--- EMAILS ---");
        testEmail("john@example.com");
        testEmail("user.name@domain.co.uk");
        testEmail("invalid@");
        testEmail("@invalid.com");

        // Test usernames
        System.out.println("\n--- USERNAMES ---");
        testUsername("john_doe");
        testUsername("user123");
        testUsername("ab");  // Trop court
        testUsername("user name");  // Espace = invalide

        // Test mots de passe
        System.out.println("\n--- MOTS DE PASSE ---");
        testPassword("Password123");  // Valide
        testPassword("weak");  // Trop court
        testPassword("nouppercase123");  // Pas de majuscule
        testPassword("NOLOWERCASE123");  // Pas de minuscule
        testPassword("NoDigits");  // Pas de chiffre

        // Test noms
        System.out.println("\n--- NOMS ---");
        testName("Jean-Pierre");
        testName("O'Connor");
        testName("A");  // Trop court
        testName("Jean123");  // Chiffres = invalide
    }

    // Méthodes de test
    private static void testEmail(String email) {
        boolean valid = isValidEmail(email);
        System.out.println(email + " → " + (valid ? "✓ Valide" : "✗ Invalide"));
    }

    private static void testUsername(String username) {
        boolean valid = isValidUsername(username);
        System.out.println(username + " → " + (valid ? "✓ Valide" : "✗ Invalide"));
    }

    private static void testPassword(String password) {
        boolean valid = isStrongPassword(password);
        String error = valid ? "✓ Valide" : "✗ " + getPasswordError(password);
        System.out.println(password + " → " + error);
    }

    private static void testName(String name) {
        boolean valid = isValidName(name);
        System.out.println(name + " → " + (valid ? "✓ Valide" : "✗ Invalide"));
    }
}

/*
 * ===== EXPLICATION DÉTAILLÉE =====
 *
 * 1. POURQUOI VALIDER LES ENTRÉES ?
 *    - Empêcher des données bizarres en base de données
 *    - Améliorer l'expérience utilisateur (messages d'erreur clairs)
 *    - Sécurité : empêcher les injections SQL (avec PreparedStatement)
 *
 * 2. QU'EST-CE QU'UNE REGEX (Expression Régulière) ?
 *    - Un "pattern" pour vérifier si un texte suit un format
 *    - Exemple : ^[A-Za-z0-9]+@[A-Za-z]+\.[A-Za-z]{2,}$
 *      → Vérifie qu'un email a bien un @ et un .com/.fr/etc.
 *
 * 3. COMMENT UTILISER DANS UN CONTROLLER ?
 *
 *    Dans UserManagementController (Adam) :
 *
 *    if (!Validator.isValidEmail(emailField.getText())) {
 *        showError("Email invalide !");
 *        return;
 *    }
 *
 *    if (!Validator.isStrongPassword(passwordField.getText())) {
 *        String error = Validator.getPasswordError(passwordField.getText());
 *        showError(error);
 *        return;
 *    }
 *
 *    // Si tout est valide, continuer...
 *
 * 4. PERSONNALISATION
 *    Vous pouvez modifier les règles selon vos besoins :
 *    - Mot de passe plus long : password.length() < 12
 *    - Username plus court : {3,15} au lieu de {3,20}
 *
 * 5. TESTER
 *    - Clic droit sur Validator.java
 *    - "Run Validator.main()"
 *    - Regardez quels exemples passent ou échouent
 */