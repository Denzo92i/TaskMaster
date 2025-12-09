package com.taskmaster.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnection {


    private static final String URL = "jdbc:mysql://localhost:3306/taskmaster_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";  // XAMPP par défaut = vide

    private static Connection connection = null;

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✓ Connexion à la base de données réussie !");
            }
            return connection;

        } catch (ClassNotFoundException e) {
            System.err.println("✗ Erreur : Driver MySQL introuvable !");
            throw new SQLException("Driver MySQL non trouvé", e);

        } catch (SQLException e) {
            System.err.println("✗ Erreur de connexion à la base de données !");
            System.err.println("Vérifiez : URL, USER, PASSWORD");
            System.err.println("Erreur : " + e.getMessage());
            throw e;
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Connexion fermée");
            }
        } catch (SQLException e) {
            System.err.println("✗ Erreur fermeture : " + e.getMessage());
        }
    }

    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}