package com.agentforge.service;

import com.agentforge.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Project management operations
 */
public interface ProjectService {
    
    /**
     * Create a new project
     * @param project the project to create
     * @return the created project
     */
    Project createProject(Project project);
    
    /**
     * Get a project by ID
     * @param id the project ID
     * @return the project if found
     */
    Optional<Project> getProjectById(Long id);
    
    /**
     * Get a project by name
     * @param name the project name
     * @return the project if found
     */
    Optional<Project> getProjectByName(String name);
    
    /**
     * Get all projects with pagination
     * @param pageable pagination parameters
     * @return page of projects
     */
    Page<Project> getAllProjects(Pageable pageable);
    
    /**
     * Get all active projects
     * @return list of active projects
     */
    List<Project> getActiveProjects();
    
    /**
     * Update an existing project
     * @param id the project ID
     * @param project the updated project data
     * @return the updated project
     */
    Project updateProject(Long id, Project project);
    
    /**
     * Delete a project
     * @param id the project ID
     */
    void deleteProject(Long id);
    
    /**
     * Archive a project
     * @param id the project ID
     * @return the archived project
     */
    Project archiveProject(Long id);
    
    /**
     * Activate a project
     * @param id the project ID
     * @return the activated project
     */
    Project activateProject(Long id);
    
    /**
     * Check if a project exists by name
     * @param name the project name
     * @return true if project exists
     */
    boolean projectExistsByName(String name);
    
    /**
     * Get project count by status
     * @return map of status to count
     */
    java.util.Map<String, Long> getProjectCountByStatus();
}
