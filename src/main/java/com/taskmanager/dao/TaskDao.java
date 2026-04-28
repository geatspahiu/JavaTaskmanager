package com.taskmanager.dao;

import com.taskmanager.db.DatabaseConnection;
import com.taskmanager.model.Priority;
import com.taskmanager.model.SortOption;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskFilter;
import com.taskmanager.model.TaskStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class TaskDao {
    private final DatabaseConnection databaseConnection;

    public TaskDao(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public void create(Task task) throws SQLException {
        String sql = """
                INSERT INTO tasks (title, description, priority, category, deadline, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindTask(statement, task);
            statement.executeUpdate();
        }
    }

    public List<Task> findAll(TaskFilter filter) throws SQLException {
        List<Object> parameters = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT id, title, description, priority, category, deadline, status
                FROM tasks
                """);

        String whereClause = buildWhereClause(filter, parameters);
        if (!whereClause.isBlank()) {
            sql.append(" WHERE ").append(whereClause);
        }
        sql.append(" ").append(orderBy(filter.getSortOption()));

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Task> tasks = new ArrayList<>();
                while (resultSet.next()) {
                    tasks.add(mapTask(resultSet));
                }
                return tasks;
            }
        }
    }

    public List<String> findCategories() throws SQLException {
        String sql = "SELECT DISTINCT category FROM tasks ORDER BY category";
        try (Connection connection = databaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            List<String> categories = new ArrayList<>();
            while (resultSet.next()) {
                categories.add(resultSet.getString("category"));
            }
            return categories;
        }
    }

    public void update(Task task) throws SQLException {
        String sql = """
                UPDATE tasks
                SET title = ?, description = ?, priority = ?, category = ?, deadline = ?, status = ?
                WHERE id = ?
                """;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindTask(statement, task);
            statement.setInt(7, task.getId());
            statement.executeUpdate();
        }
    }

    public void updateStatus(int id, TaskStatus status) throws SQLException {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.getLabel());
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private void bindTask(PreparedStatement statement, Task task) throws SQLException {
        statement.setString(1, task.getTitle());
        statement.setString(2, task.getDescription());
        statement.setString(3, task.getPriority().getLabel());
        statement.setString(4, task.getCategory());
        statement.setDate(5, Date.valueOf(task.getDeadline()));
        statement.setString(6, task.getStatus().getLabel());
    }

    private String buildWhereClause(TaskFilter filter, List<Object> parameters) {
        StringJoiner conditions = new StringJoiner(" AND ");
        if (filter.getStatus() != null) {
            conditions.add("status = ?");
            parameters.add(filter.getStatus().getLabel());
        }
        if (filter.getPriority() != null) {
            conditions.add("priority = ?");
            parameters.add(filter.getPriority().getLabel());
        }
        if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
            conditions.add("category = ?");
            parameters.add(filter.getCategory());
        }
        if (filter.getTitleSearch() != null && !filter.getTitleSearch().isBlank()) {
            conditions.add("title LIKE ?");
            parameters.add("%" + filter.getTitleSearch().trim() + "%");
        }
        return conditions.toString();
    }

    private String orderBy(SortOption sortOption) {
        return switch (sortOption) {
            case DEADLINE_ASC -> "ORDER BY deadline ASC, id DESC";
            case DEADLINE_DESC -> "ORDER BY deadline DESC, id DESC";
            case PRIORITY_ASC -> "ORDER BY FIELD(priority, 'Low', 'Medium', 'High'), deadline ASC";
            case PRIORITY_DESC -> "ORDER BY FIELD(priority, 'High', 'Medium', 'Low'), deadline ASC";
            case NONE -> "ORDER BY id DESC";
        };
    }

    private Task mapTask(ResultSet resultSet) throws SQLException {
        return new Task(
                resultSet.getInt("id"),
                resultSet.getString("title"),
                resultSet.getString("description"),
                Priority.fromLabel(resultSet.getString("priority")),
                resultSet.getString("category"),
                resultSet.getDate("deadline").toLocalDate(),
                TaskStatus.fromLabel(resultSet.getString("status"))
        );
    }
}
