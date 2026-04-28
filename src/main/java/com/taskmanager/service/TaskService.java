package com.taskmanager.service;

import com.taskmanager.dao.TaskDao;
import com.taskmanager.model.SortOption;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskFilter;
import com.taskmanager.model.TaskStats;
import com.taskmanager.model.TaskStatus;

import java.sql.SQLException;
import java.util.List;

public class TaskService {
    private final TaskDao taskDao;

    public TaskService(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    public void createTask(Task task) throws SQLException {
        validate(task);
        taskDao.create(task);
    }

    public List<Task> findTasks(TaskFilter filter) throws SQLException {
        return taskDao.findAll(filter);
    }

    public List<String> findCategories() throws SQLException {
        return taskDao.findCategories();
    }

    public TaskStats calculateAllStats() throws SQLException {
        List<Task> tasks = taskDao.findAll(new TaskFilter(null, null, null, null, SortOption.NONE));
        return calculateStats(tasks);
    }

    public void updateTask(Task task) throws SQLException {
        validate(task);
        taskDao.update(task);
    }

    public void deleteTask(int id) throws SQLException {
        taskDao.delete(id);
    }

    public void markCompleted(int id) throws SQLException {
        taskDao.updateStatus(id, TaskStatus.COMPLETED);
    }

    public void markPending(int id) throws SQLException {
        taskDao.updateStatus(id, TaskStatus.PENDING);
    }

    public TaskStats calculateStats(List<Task> tasks) {
        int completed = 0;
        int overdue = 0;
        for (Task task : tasks) {
            if (task.getStatus() == TaskStatus.COMPLETED) {
                completed++;
            }
            if (task.isOverdue()) {
                overdue++;
            }
        }
        return new TaskStats(tasks.size(), completed, overdue);
    }

    private void validate(Task task) {
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required.");
        }
        if (task.getCategory() == null || task.getCategory().isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (task.getDeadline() == null) {
            throw new IllegalArgumentException("Deadline is required.");
        }
    }
}
