package com.agentforge.entity;

/**
 * User role enumeration for AgentForge system
 */
public enum UserRole {
    ADMIN("Administrator"),
    DEVELOPER("Developer"),
    VIEWER("Viewer");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
