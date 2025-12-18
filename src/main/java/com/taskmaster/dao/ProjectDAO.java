package com.taskmaster.dao;

import com.taskmaster.models.Project;
import com.taskmaster.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour gérer les opérations sur la table 'projects'
 *
 * RESPONSABLE : René Michel
 */
public class ProjectDAO {

    /**
     * CREATE - Crée un nouveau projet avec manager_id
     */
    public boolean create(Project project) {
        String sql = "INSERT INTO projects (name, description, start_date, end_date, status, manager_id, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, project.getName());
            stmt.setString(2, project.getDescription());
            stmt.setDate(3, Date.valueOf(project.getStartDate()));
            stmt.setDate(4, project.getEndDate() != null ? Date.valueOf(project.getEndDate()) : null);
            stmt.setString(5, project.getStatus());

            // Gérer manager_id (peut être null)
            if (project.getManagerId() != null && project.getManagerId() > 0) {
                stmt.setInt(6, project.getManagerId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setInt(7, project.getCreatedBy());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    project.setId(rs.getInt(1));
                }
                return true;
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Erreur création projet : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * READ - Récupère tous les projets
     */
    public List<Project> findAll() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                projects.add(extractProjectFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération projets : " + e.getMessage());
        }

        return projects;
    }

    /**
     * READ - Trouve un projet par ID
     */
    public Project findById(int id) {
        String sql = "SELECT * FROM projects WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractProjectFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Erreur recherche projet : " + e.getMessage());
        }

        return null;
    }

    /**
     * READ - Recherche des projets par nom ou statut
     */
    public List<Project> search(String searchTerm, String status) {
        List<Project> projects = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM projects WHERE name LIKE ?");

        if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
            sql.append(" AND status = ?");
        }

        sql.append(" ORDER BY created_at DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setString(1, "%" + searchTerm + "%");

            if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
                stmt.setString(2, status);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                projects.add(extractProjectFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur recherche : " + e.getMessage());
        }

        return projects;
    }

    /**
     * READ - Obtient les projets par statut
     */
    public List<Project> findByStatus(String status) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects WHERE status = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                projects.add(extractProjectFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        return projects;
    }

    /**
     * UPDATE - Met à jour un projet
     */
    public boolean update(Project project) {
        String sql = "UPDATE projects SET name = ?, description = ?, start_date = ?, " +
                "end_date = ?, status = ?, manager_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, project.getName());
            stmt.setString(2, project.getDescription());
            stmt.setDate(3, Date.valueOf(project.getStartDate()));
            stmt.setDate(4, project.getEndDate() != null ? Date.valueOf(project.getEndDate()) : null);
            stmt.setString(5, project.getStatus());

            // Gérer manager_id
            if (project.getManagerId() != null && project.getManagerId() > 0) {
                stmt.setInt(6, project.getManagerId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setInt(7, project.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur mise à jour : " + e.getMessage());
            return false;
        }
    }

    /**
     * DELETE - Supprime un projet (et toutes ses tâches en cascade)
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM projects WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur suppression : " + e.getMessage());
            return false;
        }
    }

    /**
     * Compte le nombre total de projets
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM projects";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Erreur comptage : " + e.getMessage());
        }

        return 0;
    }

    /**
     * Compte les projets par statut
     */
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM projects WHERE status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        return 0;
    }

    /**
     * Vérifie si un nom de projet existe déjà
     */
    public boolean nameExists(String name) {
        String sql = "SELECT COUNT(*) FROM projects WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        return false;
    }

    /**
     * Extrait un Project depuis ResultSet
     */
    private Project extractProjectFromResultSet(ResultSet rs) throws SQLException {
        Project project = new Project(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("start_date").toLocalDate(),
                rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null,
                rs.getString("status"),
                rs.getInt("created_by"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );

        // Ajouter manager_id si disponible
        try {
            int managerId = rs.getInt("manager_id");
            if (!rs.wasNull()) {
                project.setManagerId(managerId);
            }
        } catch (SQLException e) {
            // Colonne manager_id n'existe pas dans le résultat
        }

        return project;
    }
}

/*
 * ===== NOTES IMPORTANTES =====
 *
 * 1. manager_id ajouté
 *    - Permet d'assigner un chef de projet
 *    - Peut être NULL
 *
 * 2. Date.valueOf()
 *    - Convertit LocalDate → java.sql.Date
 *    - Nécessaire pour les requêtes SQL
 *
 * 3. Suppression en cascade
 *    - DELETE FROM projects → supprime aussi toutes les tâches
 *    - Défini dans la BDD (ON DELETE CASCADE)
 *
 * 4. Status possibles
 *    TODO, IN_PROGRESS, COMPLETED, ON_HOLD
 */