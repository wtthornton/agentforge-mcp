package com.agentforge.service.impl;

import com.agentforge.entity.Project;
import com.agentforge.entity.ProjectStatus;
import com.agentforge.repository.ProjectRepository;
import com.agentforge.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of ProjectService
 */
@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);
    
    private final ProjectRepository projectRepository;
    
    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }
    
    @Override
    public Project createProject(Project project) {
        logger.info("Creating new project: {}", project.getName());
        
        if (projectExistsByName(project.getName())) {
            throw new IllegalArgumentException("Project with name '" + project.getName() + "' already exists");
        }
        
        project.setStatus(ProjectStatus.ACTIVE);
        Project savedProject = projectRepository.save(project);
        
        logger.info("Successfully created project with ID: {}", savedProject.getId());
        return savedProject;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Project> getProjectById(Long id) {
        logger.debug("Fetching project by ID: {}", id);
        return projectRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Project> getProjectByName(String name) {
        logger.debug("Fetching project by name: {}", name);
        return projectRepository.findByName(name);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Project> getAllProjects(Pageable pageable) {
        logger.debug("Fetching all projects with pagination: {}", pageable);
        return projectRepository.findAll(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Project> getActiveProjects() {
        logger.debug("Fetching all active projects");
        return projectRepository.findByStatus(ProjectStatus.ACTIVE);
    }
    
    @Override
    public Project updateProject(Long id, Project projectDetails) {
        logger.info("Updating project with ID: {}", id);
        
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));
        
        // Check if name is being changed and if it conflicts with existing project
        if (!existingProject.getName().equals(projectDetails.getName()) && 
            projectExistsByName(projectDetails.getName())) {
            throw new IllegalArgumentException("Project with name '" + projectDetails.getName() + "' already exists");
        }
        
        // Update fields
        existingProject.setName(projectDetails.getName());
        existingProject.setDescription(projectDetails.getDescription());
        existingProject.setStatus(projectDetails.getStatus());
        
        Project updatedProject = projectRepository.save(existingProject);
        
        logger.info("Successfully updated project with ID: {}", updatedProject.getId());
        return updatedProject;
    }
    
    @Override
    public void deleteProject(Long id) {
        logger.info("Deleting project with ID: {}", id);
        
        if (!projectRepository.existsById(id)) {
            throw new IllegalArgumentException("Project not found with ID: " + id);
        }
        
        projectRepository.deleteById(id);
        logger.info("Successfully deleted project with ID: {}", id);
    }
    
    @Override
    public Project archiveProject(Long id) {
        logger.info("Archiving project with ID: {}", id);
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));
        
        project.setStatus(ProjectStatus.ARCHIVED);
        Project archivedProject = projectRepository.save(project);
        
        logger.info("Successfully archived project with ID: {}", archivedProject.getId());
        return archivedProject;
    }
    
    @Override
    public Project activateProject(Long id) {
        logger.info("Activating project with ID: {}", id);
        
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));
        
        project.setStatus(ProjectStatus.ACTIVE);
        Project activatedProject = projectRepository.save(project);
        
        logger.info("Successfully activated project with ID: {}", activatedProject.getId());
        return activatedProject;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean projectExistsByName(String name) {
        return projectRepository.existsByName(name);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getProjectCountByStatus() {
        logger.debug("Fetching project count by status");
        
        Map<String, Long> statusCounts = new HashMap<>();
        
        for (ProjectStatus status : ProjectStatus.values()) {
            long count = projectRepository.countByStatus(status);
            statusCounts.put(status.getDisplayName(), count);
        }
        
        return statusCounts;
    }
}
