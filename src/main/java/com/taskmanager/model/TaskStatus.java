package com.taskmanager.model;

public enum TaskStatus {
    PENDING("Pending"),
    COMPLETED("Completed");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static TaskStatus fromLabel(String label) {
        for (TaskStatus status : values()) {
            if (status.label.equalsIgnoreCase(label)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
