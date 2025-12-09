package com.taskmaster.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modèle représentant une tâche (table 'tasks')
 *
 * RESPONSABLE : René Michel
 */
public class Task {

    private int id;
    private String title;
    private String description;
    private int projectId;      // ID du projet parent
    private Integer assignedTo;  // ID de l'utilisateur assigné (peut être null)
    private String priority;    // LOW, MEDIUM, HIGH, URGENT
    private String status;      // TODO, IN_PROGRESS, COMPLETED, CANCELLED
    private LocalDate dueDate;  // Date limite
    private int createdBy;      // ID du créateur
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Champs supplémentaires (non en BDD, mais utiles pour l'affichage)
    private String projectName;      // Nom du projet (pour affichage)
    private String assignedToName;   // Nom de l'utilisateur assigné

    // ===== CONSTRUCTEURS =====

    public Task() {}

    /**
     * Constructeur complet
     */
    public Task(int id, String title, String description, int projectId,
                Integer assignedTo, String priority, String status, LocalDate dueDate,
                int createdBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.projectId = projectId;
        this.assignedTo = assignedTo;
        this.priority = priority;
        this.status = status;
        this.dueDate = dueDate;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Constructeur pour créer une nouvelle tâche
     */
    public Task(String title, String description, int projectId, Integer assignedTo,
                String priority, LocalDate dueDate, int createdBy) {
        this.title = title;
        this.description = description;
        this.projectId = projectId;
        this.assignedTo = assignedTo;
        this.priority = priority;
        this.status = "TODO";
        this.dueDate = dueDate;
        this.createdBy = createdBy;
    }

    // ===== GETTERS ET SETTERS =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public Integer getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Integer assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    // ===== MÉTHODES UTILES =====

    /**
     * Vérifie si la tâche est terminée
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    /**
     * Vérifie si la tâche est en cours
     */
    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }

    /**
     * Vérifie si la tâche est urgente
     */
    public boolean isUrgent() {
        return "URGENT".equals(priority);
    }

    /**
     * Vérifie si la date limite est dépassée
     */
    public boolean isOverdue() {
        if (dueDate == null || isCompleted()) {
            return false;
        }
        return LocalDate.now().isAfter(dueDate);
    }

    /**
     * Calcule le nombre de jours restants
     * @return jours restants (négatif si en retard)
     */
    public long getDaysRemaining() {
        if (dueDate == null) {
            return Long.MAX_VALUE;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    /**
     * Obtient une couleur selon la priorité (pour l'affichage)
     */
    public String getPriorityColor() {
        switch (priority) {
            case "URGENT": return "#FF0000";  // Rouge
            case "HIGH": return "#FF8C00";    // Orange
            case "MEDIUM": return "#FFD700";  // Jaune
            case "LOW": return "#90EE90";     // Vert clair
            default: return "#CCCCCC";        // Gris
        }
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", assignedTo=" + assignedTo +
                ", dueDate=" + dueDate +
                '}';
    }
}

/*
 * ===== NOTES =====
 *
 * 1. Integer vs int pour assignedTo
 *    - int = toujours un nombre (ne peut pas être null)
 *    - Integer = peut être null (tâche non assignée)
 *
 * 2. Priorités
 *    - URGENT : À faire immédiatement
 *    - HIGH : Haute priorité
 *    - MEDIUM : Priorité normale
 *    - LOW : Basse priorité
 *
 * 3. Status
 *    - TODO : À faire
 *    - IN_PROGRESS : En cours
 *    - COMPLETED : Terminée
 *    - CANCELLED : Annulée
 *
 * 4. Champs supplémentaires (projectName, assignedToName)
 *    - Pas stockés en BDD
 *    - Remplis par jointures SQL
 *    - Facilitent l'affichage dans les tableaux
 *
 * 5. Méthodes utiles
 *    - isOverdue() : Détecte les tâches en retard
 *    - getDaysRemaining() : Calcule l'urgence
 *    - getPriorityColor() : Pour colorier dans l'interface
 */