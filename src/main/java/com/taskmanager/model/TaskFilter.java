package com.taskmanager.model;

public class TaskFilter {
    private final TaskStatus status;
    private final Priority priority;
    private final String category;
    private final String titleSearch;
    private final SortOption sortOption;

    public TaskFilter(TaskStatus status, Priority priority, String category, String titleSearch, SortOption sortOption) {
        this.status = status;
        this.priority = priority;
        this.category = category;
        this.titleSearch = titleSearch;
        this.sortOption = sortOption;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getCategory() {
        return category;
    }

    public String getTitleSearch() {
        return titleSearch;
    }

    public SortOption getSortOption() {
        return sortOption;
    }
}
