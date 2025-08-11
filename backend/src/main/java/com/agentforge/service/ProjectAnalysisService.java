package com.agentforge.service;

import com.agentforge.entity.Analysis;
import com.agentforge.entity.Project;
import com.agentforge.entity.User;
import com.agentforge.repository.AnalysisRepository;
import com.agentforge.repository.ProjectRepository;
import com.agentforge.repository.EmbeddingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Project Analysis Service for AgentForge
 * 
 * This service orchestrates the complete project analysis workflow:
 * - Project initialization and setup
 * - Analysis execution and monitoring
 * - Result processing and storage
 * - Performance optimization and caching
 * - Error handling and recovery
 * 
 * @author AgentForge Team
 * @version 1.0.0
 */
@Service
@Transactional
public class ProjectAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectAnalysisService.class);
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private EmbeddingRepository embeddingRepository;

    /**
     * Initialize a new project analysis
     */
    public Analysis initializeProjectAnalysis(Project project, User user, Analysis.AnalysisType type) {
        logger.info("Initializing project analysis for project: {} by user: {} with type: {}", 
                   project.getName(), user.getUsername(), type);

        try {
            // Create new analysis record
            Analysis analysis = new Analysis();
            analysis.setProject(project);
            analysis.setUser(user);
            analysis.setType(type);
            analysis.setStatus(Analysis.AnalysisStatus.PENDING);
            analysis.setStartTime(LocalDateTime.now());
            analysis.setCreatedAt(LocalDateTime.now());

            // Save analysis
            Analysis savedAnalysis = analysisRepository.save(analysis);
            logger.info("Project analysis initialized successfully with ID: {}", savedAnalysis.getId());

            return savedAnalysis;
        } catch (Exception e) {
            logger.error("Failed to initialize project analysis for project: {}", project.getName(), e);
            throw new RuntimeException("Failed to initialize project analysis", e);
        }
    }

    /**
     * Analyze a project by name
     */
    public Analysis analyzeProject(String projectName, User user, Analysis.AnalysisType type) {
        logger.info("Analyzing project by name: {} by user: {} with type: {}", 
                   projectName, user.getUsername(), type);

        try {
            // Find project by name
            Project project = projectRepository.findByName(projectName)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectName));

            // Initialize and execute analysis
            Analysis analysis = initializeProjectAnalysis(project, user, type);
            return executeProjectAnalysis(analysis.getId());
        } catch (Exception e) {
            logger.error("Failed to analyze project by name: {}", projectName, e);
            throw new RuntimeException("Project analysis failed", e);
        }
    }

    /**
     * Execute project analysis asynchronously
     */
    public CompletableFuture<Analysis> executeProjectAnalysisAsync(Long analysisId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeProjectAnalysis(analysisId);
            } catch (Exception e) {
                logger.error("Async project analysis execution failed for analysis ID: {}", analysisId, e);
                throw new RuntimeException("Async analysis execution failed", e);
            }
        }, executorService);
    }

    /**
     * Execute project analysis synchronously
     */
    public Analysis executeProjectAnalysis(Long analysisId) {
        logger.info("Executing project analysis with ID: {}", analysisId);

        try {
            // Retrieve analysis
            Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found: " + analysisId));

            // Update status to in progress
            analysis.setStatus(Analysis.AnalysisStatus.IN_PROGRESS);
            analysis.setStartTime(LocalDateTime.now());
            analysisRepository.save(analysis);

            // Perform analysis steps
            performAnalysisSteps(analysis);

            // Mark analysis as completed
            analysis.setStatus(Analysis.AnalysisStatus.COMPLETED);
            analysis.setEndTime(LocalDateTime.now());
            analysis.setDurationSeconds(calculateDuration(analysis.getStartTime(), analysis.getEndTime()));

            // Save completed analysis
            Analysis completedAnalysis = analysisRepository.save(analysis);
            logger.info("Project analysis completed successfully for analysis ID: {}", analysisId);

            return completedAnalysis;
        } catch (Exception e) {
            logger.error("Failed to execute project analysis for analysis ID: {}", analysisId, e);
            
            // Update analysis status to failed
            try {
                Analysis analysis = analysisRepository.findById(analysisId).orElse(null);
                if (analysis != null) {
                    analysis.setStatus(Analysis.AnalysisStatus.FAILED);
                    analysis.setEndTime(LocalDateTime.now());
                    analysis.setErrorMessage(e.getMessage());
                    analysisRepository.save(analysis);
                }
            } catch (Exception saveException) {
                logger.error("Failed to save failed analysis status for analysis ID: {}", analysisId, saveException);
            }
            
            throw new RuntimeException("Project analysis execution failed", e);
        }
    }

    /**
     * Perform the actual analysis steps
     */
    private void performAnalysisSteps(Analysis analysis) {
        logger.info("Performing analysis steps for analysis ID: {}", analysis.getId());

        try {
            Project project = analysis.getProject();
            
            // Step 1: Project structure analysis
            analyzeProjectStructure(project, analysis);
            
            // Step 2: Code quality analysis
            analyzeCodeQuality(project, analysis);
            
            // Step 3: Security analysis
            analyzeSecurity(project, analysis);
            
            // Step 4: Performance analysis
            analyzePerformance(project, analysis);
            
            // Step 5: Compliance analysis
            analyzeCompliance(project, analysis);
            
            // Step 6: Generate embeddings
            generateEmbeddings(project, analysis);
            
            logger.info("Analysis steps completed successfully for analysis ID: {}", analysis.getId());
        } catch (Exception e) {
            logger.error("Failed to perform analysis steps for analysis ID: {}", analysis.getId(), e);
            throw new RuntimeException("Analysis steps execution failed", e);
        }
    }

    /**
     * Analyze project structure
     */
    private void analyzeProjectStructure(Project project, Analysis analysis) {
        logger.info("Analyzing project structure for project: {}", project.getName());
        
        try {
            // Count files and directories
            long fileCount = countProjectFiles(project);
            long directoryCount = countProjectDirectories(project);
            
            // Analyze technology stack
            String technologyStack = analyzeTechnologyStack(project);
            
            // Update project with analysis results
            project.setFilesCount(fileCount);
            project.setDirectoriesCount(directoryCount);
            project.setTechnologyStack(technologyStack);
            project.setLastAnalysisDate(LocalDateTime.now());
            
            projectRepository.save(project);
            
            logger.info("Project structure analysis completed for project: {}", project.getName());
        } catch (Exception e) {
            logger.error("Failed to analyze project structure for project: {}", project.getName(), e);
            throw new RuntimeException("Project structure analysis failed", e);
        }
    }

    /**
     * Analyze code quality
     */
    private void analyzeCodeQuality(Project project, Analysis analysis) {
        logger.info("Analyzing code quality for project: {}", project.getName());
        
        try {
            // Calculate code quality metrics
            double qualityScore = calculateCodeQualityScore(project);
            int totalViolations = countCodeQualityViolations(project);
            int criticalViolations = countCriticalViolations(project);
            
            // Update analysis with quality results
            analysis.setQualityScore(qualityScore);
            analysis.setTotalViolations(totalViolations);
            analysis.setCriticalViolations(criticalViolations);
            
            logger.info("Code quality analysis completed for project: {}", project.getName());
        } catch (Exception e) {
            logger.error("Failed to analyze code quality for project: {}", project.getName(), e);
            throw new RuntimeException("Code quality analysis failed", e);
        }
    }

    /**
     * Analyze security aspects
     */
    private void analyzeSecurity(Project project, Analysis analysis) {
        logger.info("Analyzing security for project: {}", project.getName());
        
        try {
            // Calculate security score
            double securityScore = calculateSecurityScore(project);
            int securityViolations = countSecurityViolations(project);
            
            // Update analysis with security results
            analysis.setSecurityScore(securityScore);
            analysis.setSecurityViolations(securityViolations);
            
            logger.info("Security analysis completed for project: {}", project.getName());
        } catch (Exception e) {
            logger.error("Failed to analyze security for project: {}", project.getName(), e);
            throw new RuntimeException("Security analysis failed", e);
        }
    }

    /**
     * Analyze performance aspects
     */
    private void analyzePerformance(Project project, Analysis analysis) {
        logger.info("Analyzing performance for project: {}", project.getName());
        
        try {
            // Calculate performance score
            double performanceScore = calculatePerformanceScore(project);
            int performanceIssues = countPerformanceIssues(project);
            
            // Update analysis with performance results
            analysis.setPerformanceScore(performanceScore);
            analysis.setPerformanceIssues(performanceIssues);
            
            logger.info("Performance analysis completed for project: {}", project.getName());
        } catch (Exception e) {
            logger.error("Failed to analyze performance for project: {}", project.getName(), e);
            throw new RuntimeException("Performance analysis failed", e);
        }
    }

    /**
     * Analyze compliance with standards
     */
    private void analyzeCompliance(Project project, Analysis analysis) {
        logger.info("Analyzing compliance for project: {}", project.getName());
        
        try {
            // Calculate compliance score
            double complianceScore = calculateComplianceScore(project);
            int complianceViolations = countComplianceViolations(project);
            
            // Update analysis with compliance results
            analysis.setComplianceScore(complianceScore);
            analysis.setComplianceViolations(complianceViolations);
            
            logger.info("Compliance analysis completed for project: {}", project.getName());
        } catch (Exception e) {
            logger.error("Failed to analyze compliance for project: {}", project.getName(), e);
            throw new RuntimeException("Compliance analysis failed", e);
        }
    }

    /**
     * Generate embeddings for project content
     */
    private void generateEmbeddings(Project project, Analysis analysis) {
        logger.info("Generating embeddings for project: {}", project.getName());
        
        try {
            // Generate embeddings for different content types
            generateCodeEmbeddings(project, analysis);
            generateDocumentationEmbeddings(project, analysis);
            generateConfigurationEmbeddings(project, analysis);
            
            logger.info("Embedding generation completed for project: {}", project.getName());
        } catch (Exception e) {
            logger.error("Failed to generate embeddings for project: {}", project.getName(), e);
            throw new RuntimeException("Embedding generation failed", e);
        }
    }

    /**
     * Get analysis by ID
     */
    public Optional<Analysis> getAnalysisById(Long analysisId) {
        return analysisRepository.findById(analysisId);
    }

    /**
     * Get analyses by project
     */
    public List<Analysis> getAnalysesByProject(Project project) {
        return analysisRepository.findByProjectOrderByCreatedAtDesc(project);
    }

    /**
     * Get analyses by project with pagination
     */
    public Page<Analysis> getAnalysesByProject(Project project, Pageable pageable) {
        return analysisRepository.findByProjectOrderByCreatedAtDesc(project, pageable);
    }

    /**
     * Get analyses by user
     */
    public List<Analysis> getAnalysesByUser(User user) {
        return analysisRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Get analyses by user with pagination
     */
    public Page<Analysis> getAnalysesByUser(User user, Pageable pageable) {
        return analysisRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * Get latest analysis for project
     */
    public Optional<Analysis> getLatestAnalysisByProject(Project project) {
        return analysisRepository.findFirstByProjectOrderByCreatedAtDesc(project);
    }

    /**
     * Get latest analysis for project by type
     */
    public Optional<Analysis> getLatestAnalysisByProjectAndType(Project project, Analysis.AnalysisType type) {
        return analysisRepository.findFirstByProjectAndTypeOrderByCreatedAtDesc(project, type);
    }

    /**
     * Get analyses by status
     */
    public List<Analysis> getAnalysesByStatus(Analysis.AnalysisStatus status) {
        return analysisRepository.findByStatus(status);
    }

    /**
     * Get analyses by type
     */
    public List<Analysis> getAnalysesByType(Analysis.AnalysisType type) {
        return analysisRepository.findByType(type);
    }

    /**
     * Count analyses by project
     */
    public long countAnalysesByProject(Project project) {
        return analysisRepository.countByProject(project);
    }

    /**
     * Count analyses by user
     */
    public long countAnalysesByUser(User user) {
        return analysisRepository.countByUser(user);
    }

    /**
     * Count analyses by status
     */
    public long countAnalysesByStatus(Analysis.AnalysisStatus status) {
        return analysisRepository.countByStatus(status);
    }

    /**
     * Count analyses by type
     */
    public long countAnalysesByType(Analysis.AnalysisType type) {
        return analysisRepository.countByType(type);
    }

    /**
     * Delete analysis by ID
     */
    public void deleteAnalysisById(Long analysisId) {
        analysisRepository.deleteById(analysisId);
        logger.info("Analysis deleted successfully with ID: {}", analysisId);
    }

    /**
     * Cancel running analysis
     */
    public void cancelAnalysis(Long analysisId) {
        try {
            Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found: " + analysisId));

            if (analysis.getStatus() == Analysis.AnalysisStatus.IN_PROGRESS) {
                analysis.setStatus(Analysis.AnalysisStatus.CANCELLED);
                analysis.setEndTime(LocalDateTime.now());
                analysis.setDurationSeconds(calculateDuration(analysis.getStartTime(), analysis.getEndTime()));
                analysisRepository.save(analysis);
                logger.info("Analysis cancelled successfully with ID: {}", analysisId);
            } else {
                logger.warn("Cannot cancel analysis with ID: {} - status is not IN_PROGRESS", analysisId);
            }
        } catch (Exception e) {
            logger.error("Failed to cancel analysis with ID: {}", analysisId, e);
            throw new RuntimeException("Failed to cancel analysis", e);
        }
    }

    /**
     * Retry failed analysis
     */
    public Analysis retryFailedAnalysis(Long analysisId) {
        try {
            Analysis failedAnalysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found: " + analysisId));

            if (failedAnalysis.getStatus() != Analysis.AnalysisStatus.FAILED) {
                throw new RuntimeException("Analysis is not in FAILED status");
            }

            // Create new analysis based on failed one
            Analysis newAnalysis = new Analysis();
            newAnalysis.setProject(failedAnalysis.getProject());
            newAnalysis.setUser(failedAnalysis.getUser());
            newAnalysis.setType(failedAnalysis.getType());
            newAnalysis.setStatus(Analysis.AnalysisStatus.PENDING);
            newAnalysis.setStartTime(LocalDateTime.now());
            newAnalysis.setCreatedAt(LocalDateTime.now());

            Analysis savedAnalysis = analysisRepository.save(newAnalysis);
            logger.info("Retry analysis created successfully with ID: {} for failed analysis ID: {}", 
                       savedAnalysis.getId(), analysisId);

            return savedAnalysis;
        } catch (Exception e) {
            logger.error("Failed to retry failed analysis with ID: {}", analysisId, e);
            throw new RuntimeException("Failed to retry analysis", e);
        }
    }

    /**
     * Helper methods for analysis calculations
     */
    private long countProjectFiles(Project project) {
        // Implementation for counting project files
        return 0L; // Placeholder
    }

    private long countProjectDirectories(Project project) {
        // Implementation for counting project directories
        return 0L; // Placeholder
    }

    private String analyzeTechnologyStack(Project project) {
        // Implementation for analyzing technology stack
        return "Java,Spring Boot,PostgreSQL"; // Placeholder
    }

    private double calculateCodeQualityScore(Project project) {
        // Implementation for calculating code quality score
        return 85.0; // Placeholder
    }

    private int countCodeQualityViolations(Project project) {
        // Implementation for counting code quality violations
        return 0; // Placeholder
    }

    private int countCriticalViolations(Project project) {
        // Implementation for counting critical violations
        return 0; // Placeholder
    }

    private double calculateSecurityScore(Project project) {
        // Implementation for calculating security score
        return 90.0; // Placeholder
    }

    private int countSecurityViolations(Project project) {
        // Implementation for counting security violations
        return 0; // Placeholder
    }

    private double calculatePerformanceScore(Project project) {
        // Implementation for calculating performance score
        return 88.0; // Placeholder
    }

    private int countPerformanceIssues(Project project) {
        // Implementation for counting performance issues
        return 0; // Placeholder
    }

    private double calculateComplianceScore(Project project) {
        // Implementation for calculating compliance score
        return 92.0; // Placeholder
    }

    private int countComplianceViolations(Project project) {
        // Implementation for counting compliance violations
        return 0; // Placeholder
    }

    private void generateCodeEmbeddings(Project project, Analysis analysis) {
        // Implementation for generating code embeddings
    }

    private void generateDocumentationEmbeddings(Project project, Analysis analysis) {
        // Implementation for generating documentation embeddings
    }

    private void generateConfigurationEmbeddings(Project project, Analysis analysis) {
        // Implementation for generating configuration embeddings
    }

    private long calculateDuration(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).getSeconds();
        }
        return 0L;
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        executorService.shutdown();
        logger.info("ProjectAnalysisService cleanup completed");
    }
}
