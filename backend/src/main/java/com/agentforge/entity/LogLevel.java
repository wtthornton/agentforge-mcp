package com.agentforge.entity;

/**
 * Log level enumeration for AgentForge system
 */
public enum LogLevel {
    ERROR("Error"),
    WARN("Warning"),
    INFO("Info"),
    DEBUG("Debug"),
    TRACE("Trace");

    private final String displayName;

    LogLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
