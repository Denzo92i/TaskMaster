package com.taskmaster.utils;

import com.taskmaster.models.User;

/**
 * Gère la session de l'utilisateur connecté
 * Garde en mémoire l'utilisateur actuel dans toute l'application
 *
 * RESPONSABLE : Dylan
 */
public class SessionManager {

    // L'utilisateur actuellement connecté (null si déconnecté)
    private static User currentUser = null;

    /**
     * Définit l'utilisateur connecté
     * Appelé lors de la connexion réussie
     *
     * @param user L'utilisateur qui vient de se connecter
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
        if (user != null) {
            System.out.println("✓ Session créée pour : " + user.getUsername() +
                    " (Rôle: " + user.getRole() + ")");
        }
    }

    /**
     * Récupère l'utilisateur actuellement connecté
     *
     * @return L'utilisateur connecté, ou null si personne n'est connecté
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Vérifie si un utilisateur est connecté
     *
     * @return true si quelqu'un est connecté
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Vérifie si l'utilisateur connecté est un ADMIN
     *
     * @return true si l'utilisateur est ADMIN
     */
    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }

    /**
     * Vérifie si l'utilisateur connecté est un USER
     *
     * @return true si l'utilisateur est USER
     */
    public static boolean isUser() {
        return currentUser != null && "USER".equals(currentUser.getRole());
    }

    /**
     * Déconnecte l'utilisateur actuel
     * Appelé lors du clic sur "Déconnexion"
     */
    public static void logout() {
        if (currentUser != null) {
            System.out.println("✓ Déconnexion de : " + currentUser.getUsername());
            currentUser = null;
        }
    }

    /**
     * Obtient l'ID de l'utilisateur connecté
     * Utile pour créer des tâches/projets
     *
     * @return L'ID de l'utilisateur, ou -1 si pas connecté
     */
    public static int getCurrentUserId() {
        return (currentUser != null) ? currentUser.getId() : -1;
    }

    /**
     * Obtient le nom complet de l'utilisateur connecté
     *
     * @return "Prénom Nom", ou "Invité" si pas connecté
     */
    public static String getCurrentUserFullName() {
        if (currentUser != null) {
            return currentUser.getFirstName() + " " + currentUser.getLastName();
        }
        return "Invité";
    }

    /**
     * Pour déboguer : affiche les infos de session
     */
    public static void printSessionInfo() {
        System.out.println("\n===== SESSION ACTUELLE =====");
        if (currentUser != null) {
            System.out.println("Utilisateur : " + getCurrentUserFullName());
            System.out.println("Username    : " + currentUser.getUsername());
            System.out.println("Email       : " + currentUser.getEmail());
            System.out.println("Rôle        : " + currentUser.getRole());
            System.out.println("ID          : " + currentUser.getId());
        } else {
            System.out.println("Aucun utilisateur connecté");
        }
        System.out.println("============================\n");
    }
}

/*
 * ===== EXPLICATION DÉTAILLÉE =====
 *
 * 1. QU'EST-CE QU'UNE SESSION ?
 *    - Quand vous vous connectez, l'application doit "se souvenir" de vous
 *    - La session garde ces infos en mémoire pendant que l'app tourne
 *    - Quand vous vous déconnectez → session effacée
 *
 * 2. POURQUOI CETTE CLASSE ?
 *    - Sans SessionManager :
 *      → Il faudrait passer l'utilisateur entre TOUTES les fenêtres
 *      → Compliqué et source d'erreurs
 *
 *    - Avec SessionManager :
 *      → N'importe où dans l'app : SessionManager.getCurrentUser()
 *      → Simple et propre !
 *
 * 3. COMMENT L'UTILISER ?
 *
 *    À la connexion (dans LoginController) :
 *      User user = userDAO.authenticate(username, password);
 *      SessionManager.setCurrentUser(user);
 *
 *    Dans n'importe quelle fenêtre :
 *      if (SessionManager.isAdmin()) {
 *          // Afficher les boutons admin
 *      }
 *
 *      User current = SessionManager.getCurrentUser();
 *      welcomeLabel.setText("Bonjour " + current.getFirstName());
 *
 *    À la déconnexion :
 *      SessionManager.logout();
 *      // Retourner à l'écran de login
 *
 * 4. PATRON DE CONCEPTION
 *    C'est un "Singleton" simplifié
 *    Une seule instance de session pour toute l'application
 *
 * 5. SÉCURITÉ
 *    - La session existe SEULEMENT tant que l'app tourne
 *    - Si on ferme l'app → session perdue → doit se reconnecter
 *    - Le mot de passe N'EST PAS stocké (seulement les infos de User)
 */