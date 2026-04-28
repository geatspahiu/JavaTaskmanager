package com.taskmanager.model;

public enum SortOption {
    NONE("Default"),
    DEADLINE_ASC("Deadline (Oldest First)"),
    DEADLINE_DESC("Deadline (Newest First)"),
    PRIORITY_ASC("Priority (Low to High)"),
    PRIORITY_DESC("Priority (High to Low)");

    private final String label;

    SortOption(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
