package com.taskmaster.dao;

import com.taskmaster.models.Comment;
import com.taskmaster.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour gérer les opérations sur la table 'comments'
 *
 * RESPONSABLE : René Michel (avec aide de Adam pour l'interface)
 */
public class CommentDAO {

    /**
     * CREATE - Ajoute un commentaire à une tâche
     */
    public boolean create(Comment comment) {
        String sql = "INSERT INTO comments (task_id, user_id, content) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, comment.getTaskId());
            stmt.setInt(2, comment.getUserId());
            stmt.setString(3, comment.getContent());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    comment.setId(rs.getInt(1));
                }
                return true;
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Erreur création commentaire : " + e.getMessage());
            return false;
        }
    }

    /**
     * READ - Récupère tous les commentaires d'une tâche
     * Avec le nom de l'auteur (jointure)
     */
    public List<Comment> findByTaskId(int taskId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.*, CONCAT(u.first_name, ' ', u.last_name) as author_name " +
                "FROM comments c " +
                "JOIN users u ON c.user_id = u.id " +
                "WHERE c.task_id = ? " +
                "ORDER BY c.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Comment comment = extractCommentFromResultSet(rs);
                comment.setAuthorName(rs.getString("author_name"));
                comments.add(comment);
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération commentaires : " + e.getMessage());
        }

        return comments;
    }

    /**
     * READ - Trouve un commentaire par ID
     */
    public Comment findById(int id) {
        String sql = "SELECT c.*, CONCAT(u.first_name, ' ', u.last_name) as author_name " +
                "FROM comments c " +
                "JOIN users u ON c.user_id = u.id " +
                "WHERE c.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Comment comment = extractCommentFromResultSet(rs);
                comment.setAuthorName(rs.getString("author_name"));
                return comment;
            }

        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        return null;
    }

    /**
     * READ - Récupère tous les commentaires d'un utilisateur
     */
    public List<Comment> findByUserId(int userId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.*, CONCAT(u.first_name, ' ', u.last_name) as author_name " +
                "FROM comments c " +
                "JOIN users u ON c.user_id = u.id " +
                "WHERE c.user_id = ? " +
                "ORDER BY c.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Comment comment = extractCommentFromResultSet(rs);
                comment.setAuthorName(rs.getString("author_name"));
                comments.add(comment);
            }

        } catch (SQLException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        return comments;
    }

    /**
     * DELETE - Supprime un commentaire
     * Seul l'auteur ou un ADMIN peut supprimer
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM comments WHERE id = ?";

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
     * Compte le nombre de commentaires d'une tâche
     */
    public int countByTaskId(int taskId) {
        String sql = "SELECT COUNT(*) FROM comments WHERE task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, taskId);
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
     * Extrait un Comment depuis ResultSet
     */
    private Comment extractCommentFromResultSet(ResultSet rs) throws SQLException {
        return new Comment(
                rs.getInt("id"),
                rs.getInt("task_id"),
                rs.getInt("user_id"),
                rs.getString("content"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}

/*
 * ===== NOTES =====
 *
 * 1. Simplicité
 *    - Pas d'UPDATE pour les commentaires (généralement non modifiables)
 *    - Seulement CREATE, READ, DELETE
 *
 * 2. Jointure avec users
 *    - CONCAT(u.first_name, ' ', u.last_name) → Nom complet de l'auteur
 *    - Évite une requête séparée pour chaque commentaire
 *
 * 3. Utilisation
 *    - Afficher les commentaires d'une tâche
 *    - Ajouter un nouveau commentaire
 *    - Supprimer (avec vérification des permissions côté Controller)
 */