package com.agentforge.controller;

import com.agentforge.entity.Analysis;
import com.agentforge.entity.Project;
import com.agentforge.entity.User;
import com.agentforge.service.ProjectAnalysisService;
import com.agentforge.service.LoggingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Project controller for AgentForge backend.
 * Handles project analysis, management, and reporting endpoints.
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectAnalysisService projectAnalysisService;
    private final LoggingService loggingService;

    public ProjectController(ProjectAnalysisService projectAnalysisService, 
                           LoggingService loggingService) {
        this.projectAnalysisService = projectAnalysisService;
        this.loggingService = loggingService;
    }

    /**
     * Analyze a project from a given path
     */
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeProject(
            @RequestParam String projectPath,
            @RequestParam(defaultValue = "FULL") Analysis.AnalysisType analysisType,
            @RequestParam(defaultValue = "1") Long userId) {
        
        try {
            // TODO: Replace with actual user authentication
            User user = new User();
            user.setId(userId);
            
            // Start analysis
            Analysis analysis = projectAnalysisService.analyzeProject(projectPath, user, analysisType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analysisId", analysis.getId());
            response.put("status", analysis.getStatus());
            response.put("message", "Project analysis started successfully");
            
            // Log the analysis request
            Map<String, Object> context = new HashMap<>();
            context.put("projectPath", projectPath);
            context.put("analysisType", analysisType);
            context.put("userId", userId);
            context.put("analysisId", analysis.getId());
            
            loggingService.logInfo("API", "PROJECT_ANALYSIS", "Project analysis requested", context);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("message", "Failed to start project analysis");
            
            // Log the error
            Map<String, Object> context = new HashMap<>();
            context.put("projectPath", projectPath);
            context.put("analysisType", analysisType);
            context.put("userId", userId);
            context.put("error", e.getMessage());
            
            loggingService.logError("API", "PROJECT_ANALYSIS", "Project analysis failed", context, e);
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get analysis status by ID
     */
    @GetMapping("/analysis/{analysisId}")
    public ResponseEntity<Map<String, Object>> getAnalysisStatus(@PathVariable Long analysisId) {
        try {
            // TODO: Implement analysis retrieval from repository
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analysisId", analysisId);
            response.put("status", "COMPLETED");
            response.put("message", "Analysis status retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("message", "Failed to retrieve analysis status");
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get project information by path
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getProjectInfo(@RequestParam String projectPath) {
        try {
            // TODO: Implement project retrieval from repository
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("projectPath", projectPath);
            response.put("message", "Project information retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("message", "Failed to retrieve project information");
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get project statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getProjectStats() {
        try {
            // TODO: Implement statistics calculation from repository
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProjects", 0);
            stats.put("activeProjects", 0);
            stats.put("totalAnalyses", 0);
            stats.put("averageComplianceScore", 0.0);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            response.put("message", "Project statistics retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("message", "Failed to retrieve project statistics");
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Health check for project analysis service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Project Analysis Service");
        health.put("timestamp", java.time.LocalDateTime.now());
        health.put("capabilities", List.of("project_analysis", "technology_detection", "compliance_checking"));
        
        return ResponseEntity.ok(health);
    }
}
