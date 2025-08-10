package com.agentforge.entity;

/**
 * Task status enumeration for AgentForge system
 */
public enum TaskStatus {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    REVIEW("Review"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    BLOCKED("Blocked");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
