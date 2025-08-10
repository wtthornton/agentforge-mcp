package com.agentforge.entity;

/**
 * Project status enumeration for AgentForge system
 */
public enum ProjectStatus {
    ACTIVE("Active"),
    ARCHIVED("Archived"),
    SUSPENDED("Suspended"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
