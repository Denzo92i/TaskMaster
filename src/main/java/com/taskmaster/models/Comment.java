package com.taskmaster.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modèle représentant un commentaire (table 'comments')
 *
 * RESPONSABLE : René Michel
 */
public class Comment {

    private int id;
    private int taskId;        // ID de la tâche commentée
    private int userId;        // ID de l'auteur du commentaire
    private String content;    // Contenu du commentaire
    private LocalDateTime createdAt;

    // Champs supplémentaires (pour affichage)
    private String authorName;  // Nom de l'auteur

    // ===== CONSTRUCTEURS =====

    public Comment() {}

    /**
     * Constructeur complet
     */
    public Comment(int id, int taskId, int userId, String content, LocalDateTime createdAt) {
        this.id = id;
        this.taskId = taskId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    /**
     * Constructeur pour créer un nouveau commentaire
     */
    public Comment(int taskId, int userId, String content) {
        this.taskId = taskId;
        this.userId = userId;
        this.content = content;
    }

    // ===== GETTERS ET SETTERS =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    // ===== MÉTHODES UTILES =====

    /**
     * Formate la date de création en format lisible
     * Ex: "08/12/2024 à 14:30"
     */
    public String getFormattedDate() {
        if (createdAt == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
        return createdAt.format(formatter);
    }

    /**
     * Retourne un aperçu du commentaire (50 premiers caractères)
     */
    public String getPreview() {
        if (content == null || content.isEmpty()) {
            return "";
        }
        if (content.length() <= 50) {
            return content;
        }
        return content.substring(0, 47) + "...";
    }

    /**
     * Vérifie si le commentaire a été créé aujourd'hui
     */
    public boolean isToday() {
        if (createdAt == null) {
            return false;
        }
        return createdAt.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", taskId=" + taskId +
                ", userId=" + userId +
                ", content='" + getPreview() + '\'' +
                ", createdAt=" + getFormattedDate() +
                '}';
    }
}

/*
 * ===== NOTES =====
 *
 * 1. Structure simple
 *    - Un commentaire appartient à UNE tâche
 *    - Un commentaire a UN auteur (utilisateur)
 *
 * 2. authorName
 *    - Champ non en BDD
 *    - Rempli par une jointure SQL
 *    - Évite de devoir faire une requête séparée pour chaque commentaire
 *
 * 3. Formatage de date
 *    - DateTimeFormatter = pour afficher les dates joliment
 *    - "dd/MM/yyyy 'à' HH:mm" = 08/12/2024 à 14:30
 *
 * 4. Méthodes utiles
 *    - getPreview() : Pour afficher un aperçu court
 *    - isToday() : Pour mettre en évidence les commentaires récents
 */