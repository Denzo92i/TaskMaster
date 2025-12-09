package com.taskmaster.dao;

import com.taskmaster.models.User;
import com.taskmaster.utils.DatabaseConnection;
import com.taskmaster.utils.PasswordHasher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour gérer les opérations sur la table 'users'
 *
 * RESPONSABLES : René Michel (DAO) + Dylan (authenticate)
 */
public class UserDAO {

    /**
     * AUTHENTIFICATION - Vérifie les credentials et retourne l'utilisateur
     * Utilisé dans LoginController (Dylan)
     *
     * @param username Nom d'utilisateur
     * @param password Mot de passe en clair
     * @return User si authentification réussie, null sinon
     */
    public User authenticate(String username, String password) {
        System.out.println("🔍 === DÉBUT AUTHENTIFICATION ===");
        System.out.println("🔍 Username : " + username);
        System.out.println("🔍 Password : " + password);

        String sql = "SELECT * FROM users WHERE username = ? AND is_active = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            System.out.println("✅ Connexion BDD établie");

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("✅ Utilisateur trouvé dans la BDD");

                // Utilisateur trouvé, vérifier le mot de passe
                String hashedPassword = rs.getString("password");
                System.out.println("🔐 Hash BDD COMPLET : " + hashedPassword);
                System.out.println("🔐 Longueur du hash : " + hashedPassword.length());

                boolean passwordMatch = PasswordHasher.checkPassword(password, hashedPassword);
                System.out.println("🎯 Vérification mot de passe : " + passwordMatch);

                if (passwordMatch) {
                    System.out.println("✅✅✅ AUTHENTIFICATION RÉUSSIE ✅✅✅");
                    // Mot de passe correct, créer l'objet User
                    return extractUserFromResultSet(rs);
                } else {
                    System.out.println("❌ Mot de passe incorrect");
                }
            } else {
                System.out.println("❌ Utilisateur non trouvé dans la BDD");
            }

            return null; // Authentification échouée

        } catch (SQLException e) {
            System.err.println("❌❌❌ ERREUR SQL ❌❌❌");
            System.err.println("Erreur lors de l'authentification : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * CREATE - Ajoute un nouvel utilisateur
     *
     * @param user L'utilisateur à créer
     * @return true si succès
     */
    public boolean create(User user) {
        String sql = "INSERT INTO users (first_name, last_name, email, username, password, role) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getUsername());
            stmt.setString(5, user.getPassword());  // Doit déjà être hashé !
            stmt.setString(6, user.getRole());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Récupérer l'ID généré
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
                return true;
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de l'utilisateur : " + e.getMessage());
            return false;
        }
    }

    /**
     * READ - Récupère tous les utilisateurs
     *
     * @return Liste de tous les utilisateurs
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs : " + e.getMessage());
        }

        return users;
    }

    /**
     * READ - Trouve un utilisateur par son ID
     *
     * @param id L'ID de l'utilisateur
     * @return L'utilisateur trouvé, ou null
     */
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de l'utilisateur : " + e.getMessage());
        }

        return null;
    }

    /**
     * READ - Trouve un utilisateur par username
     *
     * @param username Le nom d'utilisateur
     * @return L'utilisateur trouvé, ou null
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
        }

        return null;
    }

    /**
     * READ - Recherche des utilisateurs par critères
     *
     * @param searchTerm Terme de recherche (nom, email, username)
     * @param role Rôle à filtrer (null = tous)
     * @return Liste des utilisateurs correspondants
     */
    public List<User> search(String searchTerm, String role) {
        List<User> users = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM users WHERE " +
                        "(first_name LIKE ? OR last_name LIKE ? OR email LIKE ? OR username LIKE ?)"
        );

        if (role != null && !role.isEmpty()) {
            sql.append(" AND role = ?");
        }

        sql.append(" ORDER BY created_at DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);

            if (role != null && !role.isEmpty()) {
                stmt.setString(5, role);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
        }

        return users;
    }

    /**
     * UPDATE - Met à jour un utilisateur
     *
     * @param user L'utilisateur à mettre à jour
     * @return true si succès
     */
    public boolean update(User user) {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, " +
                "username = ?, role = ?, is_active = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getUsername());
            stmt.setString(5, user.getRole());
            stmt.setBoolean(6, user.isActive());
            stmt.setInt(7, user.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour : " + e.getMessage());
            return false;
        }
    }

    /**
     * UPDATE - Change le mot de passe d'un utilisateur
     *
     * @param userId ID de l'utilisateur
     * @param newPassword Nouveau mot de passe (DOIT être hashé avant !)
     * @return true si succès
     */
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors du changement de mot de passe : " + e.getMessage());
            return false;
        }
    }

    /**
     * DELETE - Supprime un utilisateur
     *
     * @param id L'ID de l'utilisateur à supprimer
     * @return true si succès
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            return false;
        }
    }

    /**
     * Compte le nombre total d'utilisateurs
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage : " + e.getMessage());
        }

        return 0;
    }

    /**
     * Vérifie si un email existe déjà
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
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
     * Vérifie si un username existe déjà
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
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
     * UTILITAIRE - Extrait un User depuis un ResultSet
     * Évite la duplication de code
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role"),
                rs.getBoolean("is_active"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}

/*
 * ===== EXPLICATION DÉTAILLÉE =====
 *
 * 1. QU'EST-CE QU'UN DAO ?
 *    DAO = Data Access Object
 *    Toutes les requêtes SQL pour une table sont ici
 *    Séparation propre : DAO = SQL, Controller = logique métier
 *
 * 2. PREPARED STATEMENT vs STATEMENT
 *    - PreparedStatement : TOUJOURS utiliser pour les requêtes avec paramètres
 *    - Protège contre les injections SQL
 *    - stmt.setString(1, username) → Sécurisé
 *    - "SELECT * FROM users WHERE username = '" + username + "'" → DANGER !
 *
 * 3. TRY-WITH-RESOURCES
 *    try (Connection conn = ...; PreparedStatement stmt = ...) {
 *        // Code
 *    }
 *    → Ferme automatiquement conn et stmt à la fin
 *    → Évite les fuites mémoire
 *
 * 4. UTILISATION TYPIQUE
 *
 *    // Créer un utilisateur
 *    UserDAO userDAO = new UserDAO();
 *    User user = new User("John", "Doe", "john@mail.com", "john", hashedPass, "USER");
 *    boolean success = userDAO.create(user);
 *
 *    // Authentifier
 *    User logged = userDAO.authenticate("john", "password123");
 *    if (logged != null) {
 *        SessionManager.setCurrentUser(logged);
 *    }
 *
 *    // Rechercher
 *    List<User> users = userDAO.findAll();
 *    List<User> admins = userDAO.search("", "ADMIN");
 *
 * 5. STATEMENT.RETURN_GENERATED_KEYS
 *    - Récupère l'ID auto-généré par MySQL
 *    - Permet de connaître l'ID du nouvel utilisateur créé
 */