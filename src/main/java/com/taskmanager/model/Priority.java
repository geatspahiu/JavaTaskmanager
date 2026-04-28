package com.taskmanager.model;

public enum Priority {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3);

    private final String label;
    private final int rank;

    Priority(String label, int rank) {
        this.label = label;
        this.rank = rank;
    }

    public String getLabel() {
        return label;
    }

    public int getRank() {
        return rank;
    }

    public static Priority fromLabel(String label) {
        for (Priority priority : values()) {
            if (priority.label.equalsIgnoreCase(label)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Unknown priority: " + label);
    }

    @Override
    public String toString() {
        return label;
    }
}
