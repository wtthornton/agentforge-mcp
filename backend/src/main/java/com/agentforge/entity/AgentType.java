package com.agentforge.entity;

/**
 * Agent type enumeration for AgentForge system
 */
public enum AgentType {
    CODE_ANALYZER("Code Analyzer"),
    COMPLIANCE_CHECKER("Compliance Checker"),
    PERFORMANCE_OPTIMIZER("Performance Optimizer"),
    SECURITY_SCANNER("Security Scanner"),
    DOCUMENTATION_GENERATOR("Documentation Generator"),
    TEST_GENERATOR("Test Generator"),
    REFACTORING_ASSISTANT("Refactoring Assistant"),
    DEPENDENCY_ANALYZER("Dependency Analyzer");

    private final String displayName;

    AgentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
