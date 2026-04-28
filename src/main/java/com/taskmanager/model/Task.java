package com.taskmanager.model;

import java.time.LocalDate;

public class Task {
    private int id;
    private String title;
    private String description;
    private Priority priority;
    private String category;
    private LocalDate deadline;
    private TaskStatus status;

    public Task(int id, String title, String description, Priority priority, String category,
                LocalDate deadline, TaskStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
        this.deadline = deadline;
        this.status = status;
    }

    public Task(String title, String description, Priority priority, String category,
                LocalDate deadline, TaskStatus status) {
        this(0, title, description, priority, category, deadline, status);
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public boolean isOverdue() {
        return status == TaskStatus.PENDING && deadline.isBefore(LocalDate.now());
    }
}
