package com.taskmanager.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/task_manager?createDatabaseIfNotExist=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private final String url;
    private final String user;
    private final String password;

    public DatabaseConnection() {
        this.url = System.getenv().getOrDefault("DB_URL", DEFAULT_URL);
        this.user = System.getenv().getOrDefault("DB_USER", DEFAULT_USER);
        this.password = System.getenv().getOrDefault("DB_PASSWORD", DEFAULT_PASSWORD);
        initializeDatabase();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private void initializeDatabase() {
        String sql = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(120) NOT NULL,
                    description TEXT,
                    priority ENUM('Low','Medium','High') NOT NULL DEFAULT 'Medium',
                    category VARCHAR(80) NOT NULL,
                    deadline DATE NOT NULL,
                    status ENUM('Pending','Completed') NOT NULL DEFAULT 'Pending',
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to initialize MySQL database. Check DB_URL, DB_USER, and DB_PASSWORD.", ex);
        }
    }
}
