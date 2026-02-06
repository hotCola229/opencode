package com.example.backend.enums;

public enum ProjectStatus {
    DRAFT(0),
    ACTIVE(1),
    ARCHIVED(2);

    private final int value;

    ProjectStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ProjectStatus fromValue(int value) {
        for (ProjectStatus status : ProjectStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status value: " + value);
    }
}
