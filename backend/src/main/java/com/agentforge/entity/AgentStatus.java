package com.agentforge.entity;

/**
 * Agent status enumeration for AgentForge system
 */
public enum AgentStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    RUNNING("Running"),
    STOPPED("Stopped"),
    ERROR("Error"),
    MAINTENANCE("Maintenance");

    private final String displayName;

    AgentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
