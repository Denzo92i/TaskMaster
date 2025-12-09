package com.taskmaster.dao;

import com.taskmaster.models.Task;
import com.taskmaster.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour gérer les opérations sur la table 'tasks'
 *
 * RESPONSABLE : René Michel
 */
public class TaskDAO {

    /**
     * CREATE - Crée une nouvelle tâche
     */
    public boolean create(Task task) {
        String sql = "INSERT INTO tasks (title, description, project_id, assigned_to, priority, status, due_date, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setInt(3, task.getProjectId());

            if (task.getAssignedTo() != null) {
                stmt.setInt(4, task.getAssignedTo());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setString(5, task.getPriority());
            stmt.setString(6, task.getStatus());
            stmt.setDate(7, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : null);
            stmt.setInt(8, task.getCreatedBy());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    task.setId(rs.getInt(1));
                }
                return true;
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Erreur création tâche : " + e.getMessage());
            return false;
        }
    }

    /**
     * READ - Récupère toutes les tâches (ADMIN)
     * Avec jointures pour obtenir les noms de projet et d'utilisateur
     */
    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.*, p.name as project_name, " +
                "CONCAT(u.first_name, ' ', u.last_name) as assigned_to_name " +
                "FROM tasks t " +
                "JOIN projects p ON t.project_id = p.id " +
                "LEFT JOIN users u ON t.assigned_to = u.id " +
                "ORDER BY t.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Task task = extractTaskFromResultSet(rs);
                task.setProjectName(rs.getString("project_name"));
                task.setAssignedToName(rs.getString("assigned_to_name"));
                tasks.add(task);
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération tâches : " + e.getMessage());
        }

        return tasks;
    }

    /**
     * READ - Récupère les tâches d'un utilisateur spécifique (USER)
     */
    public List<Task> findByUserId(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.*, p.name as project_name, " +
                "CONCAT(u.first_name, ' ', u.last_name) as assigned_to_name " +
                "FROM tasks t " +
                "JOIN projects p ON t.project_id = p.id " +
                "LEFT JOIN users u ON t.assigned_to = u.id " +
                "WHERE t.assigned_to = ? " +
                "ORDER BY t.due_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Task task = extractTaskFromResultSet(rs);
                task.setProjectName(rs.getString("project_name"));
                task.setAssignedToName(rs.getString("assigned_to_name"));
                tasks.add(task);
            }

        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        return tasks;
    }

    /**
     * READ - Trouve une tâche par ID
     */
    public Task findById(int id) {
        String sql = "SELECT t.*, p.name as project_name, " +
                "CONCAT(u.first_name, ' ', u.last_name) as assigned_to_name " +
                "FROM tasks t " +
                "JOIN projects p ON t.project_id = p.id " +
                "LEFT JOIN users u ON t.assigned_to = u.id " +
                "WHERE t.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Task task = extractTaskFromResultSet(rs);
                task.setProjectName(rs.getString("project_name"));
                task.setAssignedToName(rs.getString("assigned_to_name"));
                return task;
            }

        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        return null;
    }

    /**
     * READ - Recherche de tâches avec filtres multiples
     */
    public List<Task> search(String searchTerm, String status, String priority, Integer projectId) {
        List<Task> tasks = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT t.*, p.name as project_name, " +
                        "CONCAT(u.first_name, ' ', u.last_name) as assigned_to_name " +
                        "FROM tasks t " +
                        "JOIN projects p ON t.project_id = p.id " +
                        "LEFT JOIN users u ON t.assigned_to = u.id " +
                        "WHERE t.title LIKE ?"
        );

        if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
            sql.append(" AND t.status = ?");
        }
        if (priority != null && !priority.isEmpty() && !"ALL".equals(priority)) {
            sql.append(" AND t.priority = ?");
        }
        if (projectId != null && projectId > 0) {
            sql.append(" AND t.project_id = ?");
        }

        sql.append(" ORDER BY t.created_at DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, "%" + searchTerm + "%");

            if (status != null && !status.isEmpty() && !"ALL".equals(status)) {
                stmt.setString(paramIndex++, status);
            }
            if (priority != null && !priority.isEmpty() && !"ALL".equals(priority)) {
                stmt.setString(paramIndex++, priority);
            }
            if (projectId != null && projectId > 0) {
                stmt.setInt(paramIndex++, projectId);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Task task = extractTaskFromResultSet(rs);
                task.setProjectName(rs.getString("project_name"));
                task.setAssignedToName(rs.getString("assigned_to_name"));
                tasks.add(task);
            }

        } catch (SQLException e) {
            System.err.println("Erreur recherche : " + e.getMessage());
        }

        return tasks;
    }

    /**
     * READ - Tâches d'un projet spécifique
     */
    public List<Task> findByProjectId(int projectId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT t.*, p.name as project_name, " +
                "CONCAT(u.first_name, ' ', u.last_name) as assigned_to_name " +
                "FROM tasks t " +
                "JOIN projects p ON t.project_id = p.id " +
                "LEFT JOIN users u ON t.assigned_to = u.id " +
                "WHERE t.project_id = ? " +
                "ORDER BY t.due_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Task task = extractTaskFromResultSet(rs);
                task.setProjectName(rs.getString("project_name"));
                task.setAssignedToName(rs.getString("assigned_to_name"));
                tasks.add(task);
            }

        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        return tasks;
    }

    /**
     * UPDATE - Met à jour une tâche (ADMIN)
     */
    public boolean update(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, project_id = ?, assigned_to = ?, " +
                "priority = ?, status = ?, due_date = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setInt(3, task.getProjectId());

            if (task.getAssignedTo() != null) {
                stmt.setInt(4, task.getAssignedTo());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setString(5, task.getPriority());
            stmt.setString(6, task.getStatus());
            stmt.setDate(7, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : null);
            stmt.setInt(8, task.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur mise à jour : " + e.getMessage());
            return false;
        }
    }

    /**
     * UPDATE - Met à jour seulement le statut (USER peut faire ça)
     */
    public boolean updateStatus(int taskId, String newStatus) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, taskId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
            return false;
        }
    }

    /**
     * DELETE - Supprime une tâche
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";

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
     * Compte toutes les tâches
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM tasks";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        return 0;
    }

    /**
     * Compte les tâches par statut
     */
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE status = ?";

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
     * Extrait une Task depuis ResultSet
     */
    private Task extractTaskFromResultSet(ResultSet rs) throws SQLException {
        return new Task(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("project_id"),
                (Integer) rs.getObject("assigned_to"),
                rs.getString("priority"),
                rs.getString("status"),
                rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null,
                rs.getInt("created_by"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}

/*
 * ===== NOTES =====
 *
 * 1. JOINTURES SQL
 *    - JOIN projects : récupère le nom du projet
 *    - LEFT JOIN users : récupère le nom de l'utilisateur assigné
 *    - LEFT (vs JOIN) : garde la tâche même si pas assignée
 *
 * 2. assigned_to NULL
 *    - Une tâche peut ne pas être assignée
 *    - stmt.setNull(4, Types.INTEGER) pour insérer NULL
 *
 * 3. Recherche multi-critères
 *    - Permet de combiner : titre + statut + priorité + projet
 *    - Utile pour les filtres complexes dans l'interface
 */