package com.taskmaster.models;

import java.time.LocalDateTime;

/**
 * Modèle représentant un utilisateur (table 'users')
 *
 * RESPONSABLE : René Michel
 */
public class User {

    // Attributs correspondant aux colonnes de la table 'users'
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;  // Hashé (jamais en clair !)
    private String role;      // "ADMIN" ou "USER"
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== CONSTRUCTEURS =====

    /**
     * Constructeur vide (nécessaire pour certaines opérations)
     */
    public User() {}

    /**
     * Constructeur complet (utilisé lors de la récupération depuis la BDD)
     */
    public User(int id, String firstName, String lastName, String email,
                String username, String password, String role, boolean isActive,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Constructeur pour créer un nouvel utilisateur (sans ID, dates auto)
     */
    public User(String firstName, String lastName, String email,
                String username, String password, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
        this.isActive = true;
    }

    // ===== GETTERS ET SETTERS =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ===== MÉTHODES UTILES =====

    /**
     * Retourne le nom complet de l'utilisateur
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Vérifie si l'utilisateur est un administrateur
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    /**
     * Vérifie si l'utilisateur est un utilisateur standard
     */
    public boolean isUser() {
        return "USER".equals(role);
    }

    /**
     * Représentation textuelle de l'utilisateur (pour debug)
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}

/*
 * ===== EXPLICATION DÉTAILLÉE =====
 *
 * 1. QU'EST-CE QU'UN MODÈLE ?
 *    - Représente une ligne de la table 'users'
 *    - Chaque attribut = une colonne de la table
 *    - Facilite le passage de données entre la BDD et l'interface
 *
 * 2. POURQUOI DES GETTERS/SETTERS ?
 *    - En Java, on encapsule les attributs (principe POO)
 *    - Au lieu de : user.firstName = "John"
 *    - On fait : user.setFirstName("John")
 *    - Permet d'ajouter de la logique plus tard (validation, etc.)
 *
 * 3. LES CONSTRUCTEURS
 *    - Constructeur vide : User user = new User();
 *    - Constructeur complet : pour créer depuis la BDD
 *    - Constructeur partiel : pour créer un nouvel utilisateur
 *
 * 4. UTILISATION TYPIQUE
 *
 *    Créer un utilisateur :
 *      User user = new User("John", "Doe", "john@mail.com",
 *                           "john", hashedPassword, "USER");
 *
 *    Modifier un attribut :
 *      user.setEmail("newemail@mail.com");
 *
 *    Récupérer des infos :
 *      String name = user.getFullName();  // "John Doe"
 *      boolean admin = user.isAdmin();    // false
 *
 * 5. LocalDateTime vs Date
 *    - LocalDateTime = classe moderne de Java 8+
 *    - Meilleure que l'ancienne classe Date
 *    - Format : 2024-12-08T14:30:00
 */