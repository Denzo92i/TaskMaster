package com.taskmaster.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modèle représentant un projet (table 'projects')
 *
 * RESPONSABLE : René Michel
 */
public class Project {

    private int id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;  // TODO, IN_PROGRESS, COMPLETED, ON_HOLD
    private Integer managerId;  // ID du chef de projet (peut être null)
    private int createdBy;  // ID de l'utilisateur créateur
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== CONSTRUCTEURS =====

    public Project() {}

    /**
     * Constructeur complet (récupération depuis BDD)
     */
    public Project(int id, String name, String description, LocalDate startDate,
                   LocalDate endDate, String status, int createdBy,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Constructeur pour créer un nouveau projet
     */
    public Project(String name, String description, LocalDate startDate,
                   LocalDate endDate, int createdBy) {
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = "TODO";
        this.createdBy = createdBy;
    }

    // ===== GETTERS ET SETTERS =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
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

    // ===== MÉTHODES UTILES =====

    /**
     * Vérifie si le projet est en cours
     */
    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }

    /**
     * Vérifie si le projet est terminé
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    /**
     * Calcule la durée du projet en jours
     */
    public long getDurationInDays() {
        if (startDate != null && endDate != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", managerId=" + managerId +
                '}';
    }
}

/*
 * ===== NOTES =====
 *
 * 1. LocalDate vs LocalDateTime
 *    - LocalDate = Seulement la date (2024-12-08)
 *    - LocalDateTime = Date + heure (2024-12-08 14:30:00)
 *    - Ici : startDate/endDate = LocalDate (on ne veut que la date)
 *
 * 2. Status possibles
 *    TODO, IN_PROGRESS, COMPLETED, ON_HOLD
 *
 * 3. managerId (NOUVEAU)
 *    - Integer (peut être null)
 *    - Référence à un utilisateur qui gère le projet
 *
 * 4. createdBy
 *    - Stocke l'ID de l'utilisateur qui a créé le projet
 *    - Permet de savoir qui est responsable
 */