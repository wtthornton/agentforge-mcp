package com.agentforge.service;

import com.agentforge.entity.Analysis;
import com.agentforge.entity.Project;
import com.agentforge.entity.User;
import com.agentforge.repository.AnalysisRepository;
import com.agentforge.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Project analysis service for AgentForge backend.
 * Analyzes project structure, code quality, and provides insights.
 */
@Service
@Transactional
public class ProjectAnalysisService {

    private final LoggingService loggingService;
    private final ProjectRepository projectRepository;
    private final AnalysisRepository analysisRepository;
    
    // File extensions to analyze
    private static final Set<String> CODE_EXTENSIONS = Set.of(
        "java", "ts", "tsx", "js", "jsx", "py", "go", "rs", "cpp", "c", "cs", "php", "rb"
    );
    
    private static final Set<String> CONFIG_EXTENSIONS = Set.of(
        "xml", "yml", "yaml", "json", "properties", "toml", "gradle", "pom.xml"
    );
    
    private static final Set<String> DOC_EXTENSIONS = Set.of(
        "md", "txt", "rst", "adoc", "html", "css", "scss"
    );

    public ProjectAnalysisService(LoggingService loggingService, 
                                ProjectRepository projectRepository,
                                AnalysisRepository analysisRepository) {
        this.loggingService = loggingService;
        this.projectRepository = projectRepository;
        this.analysisRepository = analysisRepository;
    }

    /**
     * Analyze project structure and provide insights
     */
    public Analysis analyzeProject(String projectPath, User user, Analysis.AnalysisType type) {
        Analysis analysis = new Analysis();
        analysis.setProject(null); // Will be set after project creation/retrieval
        analysis.setUser(user);
        analysis.setType(type);
        analysis.startAnalysis();
        
        try {
            Path path = Paths.get(projectPath);
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("Project path does not exist: " + projectPath);
            }
            
            // Create or retrieve project
            Project project = findOrCreateProject(projectPath, user);
            analysis.setProject(project);
            
            // Basic project information
            Map<String, Object> analysisData = new HashMap<>();
            analysisData.put("projectPath", projectPath);
            analysisData.put("analyzedAt", LocalDateTime.now());
            
            // File structure analysis
            Map<String, Object> fileStructure = analyzeFileStructure(path);
            analysisData.put("fileStructure", fileStructure);
            
            // Technology stack detection
            Map<String, Object> techStack = detectTechnologyStack(path);
            analysisData.put("technologyStack", techStack);
            
            // Code quality indicators
            Map<String, Object> codeQuality = analyzeCodeQuality(path);
            analysisData.put("codeQuality", codeQuality);
            
            // Project complexity metrics
            Map<String, Object> complexity = analyzeProjectComplexity(path);
            analysisData.put("complexity", complexity);
            
            // Standards compliance indicators
            Map<String, Object> standards = analyzeStandardsCompliance(path);
            analysisData.put("standards", standards);
            
            // Generate insights
            Map<String, Object> insights = generateProjectInsights(analysisData);
            analysisData.put("insights", insights);
            
            // Update project with analysis results
            updateProjectWithAnalysis(project, analysisData);
            
            // Set analysis metrics
            setAnalysisMetrics(analysis, analysisData);
            
            // Complete analysis
            analysis.completeAnalysis();
            
            // Save analysis to database
            analysis = analysisRepository.save(analysis);
            
            // Log analysis completion
            Map<String, Object> context = new HashMap<>();
            context.put("projectPath", projectPath);
            context.put("analysisId", analysis.getId());
            context.put("totalFiles", fileStructure.get("totalFiles"));
            context.put("codeFiles", fileStructure.get("codeFiles"));
            
            loggingService.logInfo("ANALYSIS", "PROJECT", "Project analysis completed successfully", context);
            
        } catch (Exception e) {
            analysis.failAnalysis(e.getMessage());
            analysis = analysisRepository.save(analysis);
            
            Map<String, Object> context = new HashMap<>();
            context.put("projectPath", projectPath);
            context.put("error", e.getMessage());
            
            loggingService.logError("ANALYSIS", "PROJECT", "Project analysis failed", context, e);
        }
        
        return analysis;
    }

    /**
     * Find existing project or create new one
     */
    private Project findOrCreateProject(String projectPath, User user) {
        return projectRepository.findByProjectPath(projectPath)
                .orElseGet(() -> {
                    Project newProject = new Project();
                    newProject.setName(Paths.get(projectPath).getFileName().toString());
                    newProject.setProjectPath(projectPath);
                    newProject.setOwner(user);
                    newProject.setDescription("Auto-created project for analysis");
                    newProject.setStatus(Project.ProjectStatus.ACTIVE);
                    newProject.setTechnologyStack("Unknown");
                    newProject.setLinesOfCode(0L);
                    newProject.setLastAnalysisDate(LocalDateTime.now());
                    
                    return projectRepository.save(newProject);
                });
    }

    /**
     * Update project with analysis results
     */
    private void updateProjectWithAnalysis(Project project, Map<String, Object> analysisData) {
        Map<String, Object> techStack = (Map<String, Object>) analysisData.get("technologyStack");
        Map<String, Object> fileStructure = (Map<String, Object>) analysisData.get("fileStructure");
        
        // Update technology stack
        List<String> technologies = (List<String>) techStack.get("detectedTechnologies");
        if (technologies != null && !technologies.isEmpty()) {
            project.setTechnologyStack(String.join(", ", technologies));
        }
        
        // Update lines of code
        Integer codeFiles = (Integer) fileStructure.get("codeFiles");
        if (codeFiles != null) {
            project.setLinesOfCode(codeFiles.longValue());
        }
        
        // Update last analysis date
        project.updateLastAnalysisDate();
        
        projectRepository.save(project);
    }

    /**
     * Set analysis metrics from analysis data
     */
    private void setAnalysisMetrics(Analysis analysis, Map<String, Object> analysisData) {
        Map<String, Object> fileStructure = (Map<String, Object>) analysisData.get("fileStructure");
        Map<String, Object> standards = (Map<String, Object>) analysisData.get("standards");
        Map<String, Object> complexity = (Map<String, Object>) analysisData.get("complexity");
        
        // Set file counts
        Integer totalFiles = (Integer) fileStructure.get("totalFiles");
        Integer codeFiles = (Integer) fileStructure.get("codeFiles");
        if (totalFiles != null) analysis.setFilesAnalyzed(totalFiles.longValue());
        if (codeFiles != null) analysis.setLinesAnalyzed(codeFiles.longValue());
        
        // Set compliance score
        Integer complianceScore = (Integer) standards.get("complianceScore");
        if (complianceScore != null) {
            analysis.setComplianceScore(complianceScore.doubleValue());
        }
        
        // Set complexity score
        Double complexityScore = (Double) complexity.get("complexityScore");
        if (complexityScore != null) {
            analysis.setPerformanceScore(complexityScore);
        }
        
        // Set summary
        Map<String, Object> insights = (Map<String, Object>) analysisData.get("insights");
        String overallAssessment = (String) insights.get("overallAssessment");
        analysis.setAnalysisSummary("Analysis completed with overall assessment: " + overallAssessment);
    }

    /**
     * Analyze file structure of the project
     */
    private Map<String, Object> analyzeFileStructure(Path projectPath) throws IOException {
        Map<String, Object> structure = new HashMap<>();
        
        final int[] totalFiles = {0};
        final int[] codeFiles = {0};
        final int[] configFiles = {0};
        final int[] docFiles = {0};
        final Map<String, Integer> extensionCounts = new HashMap<>();
        final List<String> directories = new ArrayList<>();
        
        Files.walkFileTree(projectPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                totalFiles[0]++;
                
                String fileName = file.getFileName().toString();
                String extension = getFileExtension(fileName);
                
                if (CODE_EXTENSIONS.contains(extension)) {
                    codeFiles[0]++;
                } else if (CONFIG_EXTENSIONS.contains(extension)) {
                    configFiles[0]++;
                } else if (DOC_EXTENSIONS.contains(extension)) {
                    docFiles[0]++;
                }
                
                extensionCounts.merge(extension, 1, Integer::sum);
                
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (!dir.equals(projectPath)) {
                    directories.add(dir.getFileName().toString());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        
        structure.put("totalFiles", totalFiles[0]);
        structure.put("codeFiles", codeFiles[0]);
        structure.put("configFiles", configFiles[0]);
        structure.put("docFiles", docFiles[0]);
        structure.put("directories", directories);
        structure.put("extensionCounts", extensionCounts);
        
        return structure;
    }

    /**
     * Detect technology stack used in the project
     */
    private Map<String, Object> detectTechnologyStack(Path projectPath) {
        Map<String, Object> techStack = new HashMap<>();
        List<String> detectedTechnologies = new ArrayList<>();
        Map<String, String> versionInfo = new HashMap<>();
        
        // Check for common project files
        if (Files.exists(projectPath.resolve("pom.xml"))) {
            detectedTechnologies.add("Maven");
            techStack.put("buildTool", "Maven");
        } else if (Files.exists(projectPath.resolve("build.gradle"))) {
            detectedTechnologies.add("Gradle");
            techStack.put("buildTool", "Gradle");
        }
        
        if (Files.exists(projectPath.resolve("package.json"))) {
            detectedTechnologies.add("Node.js");
            techStack.put("runtime", "Node.js");
        }
        
        if (Files.exists(projectPath.resolve("requirements.txt"))) {
            detectedTechnologies.add("Python");
            techStack.put("runtime", "Python");
        }
        
        if (Files.exists(projectPath.resolve("Cargo.toml"))) {
            detectedTechnologies.add("Rust");
            techStack.put("runtime", "Rust");
        }
        
        if (Files.exists(projectPath.resolve("go.mod"))) {
            detectedTechnologies.add("Go");
            techStack.put("runtime", "Go");
        }
        
        // Check for framework indicators
        if (Files.exists(projectPath.resolve("src/main/java"))) {
            detectedTechnologies.add("Java");
            techStack.put("language", "Java");
        }
        
        if (Files.exists(projectPath.resolve("src/main/kotlin"))) {
            detectedTechnologies.add("Kotlin");
            techStack.put("language", "Kotlin");
        }
        
        if (Files.exists(projectPath.resolve("src"))) {
            if (Files.exists(projectPath.resolve("src").resolve("*.ts"))) {
                detectedTechnologies.add("TypeScript");
                techStack.put("language", "TypeScript");
            } else if (Files.exists(projectPath.resolve("src").resolve("*.js"))) {
                detectedTechnologies.add("JavaScript");
                techStack.put("language", "JavaScript");
            }
        }
        
        techStack.put("detectedTechnologies", detectedTechnologies);
        techStack.put("versionInfo", versionInfo);
        
        return techStack;
    }

    /**
     * Analyze code quality indicators
     */
    private Map<String, Object> analyzeCodeQuality(Path projectPath) {
        Map<String, Object> quality = new HashMap<>();
        
        // TODO: Implement actual code quality analysis
        // This would include:
        // - Cyclomatic complexity
        // - Code duplication detection
        // - Naming convention compliance
        // - Documentation coverage
        
        quality.put("cyclomaticComplexity", "UNKNOWN");
        quality.put("codeDuplication", "UNKNOWN");
        quality.put("namingConventions", "UNKNOWN");
        quality.put("documentationCoverage", "UNKNOWN");
        quality.put("codeSmells", "UNKNOWN");
        
        return quality;
    }

    /**
     * Analyze project complexity
     */
    private Map<String, Object> analyzeProjectComplexity(Path projectPath) {
        Map<String, Object> complexity = new HashMap<>();
        
        try {
            // Count files by type
            long totalFiles = Files.walk(projectPath)
                    .filter(Files::isRegularFile)
                    .count();
            
            long codeFiles = Files.walk(projectPath)
                    .filter(Files::isRegularFile)
                    .filter(file -> CODE_EXTENSIONS.contains(getFileExtension(file.getFileName().toString())))
                    .count();
            
            // Calculate complexity score (simplified)
            double complexityScore = calculateComplexityScore(totalFiles, codeFiles);
            
            complexity.put("totalFiles", totalFiles);
            complexity.put("codeFiles", codeFiles);
            complexity.put("complexityScore", complexityScore);
            complexity.put("complexityLevel", getComplexityLevel(complexityScore));
            
        } catch (IOException e) {
            complexity.put("error", "Failed to analyze complexity: " + e.getMessage());
        }
        
        return complexity;
    }

    /**
     * Analyze standards compliance indicators
     */
    private Map<String, Object> analyzeStandardsCompliance(Path projectPath) {
        Map<String, Object> standards = new HashMap<>();
        
        // Check for common standards files
        boolean hasReadme = Files.exists(projectPath.resolve("README.md")) || 
                           Files.exists(projectPath.resolve("README.txt"));
        
        boolean hasLicense = Files.walk(projectPath)
                .anyMatch(file -> file.getFileName().toString().toLowerCase().contains("license"));
        
        boolean hasContributing = Files.exists(projectPath.resolve("CONTRIBUTING.md")) ||
                                 Files.exists(projectPath.resolve("CONTRIBUTING.txt"));
        
        boolean hasChangelog = Files.exists(projectPath.resolve("CHANGELOG.md")) ||
                              Files.exists(projectPath.resolve("CHANGELOG.txt"));
        
        standards.put("hasReadme", hasReadme);
        standards.put("hasLicense", hasLicense);
        standards.put("hasContributing", hasContributing);
        standards.put("hasChangelog", hasChangelog);
        
        // Calculate compliance score
        int complianceScore = 0;
        if (hasReadme) complianceScore += 25;
        if (hasLicense) complianceScore += 25;
        if (hasContributing) complianceScore += 25;
        if (hasChangelog) complianceScore += 25;
        
        standards.put("complianceScore", complianceScore);
        standards.put("complianceLevel", getComplianceLevel(complianceScore));
        
        return standards;
    }

    /**
     * Generate project insights based on analysis
     */
    private Map<String, Object> generateProjectInsights(Map<String, Object> analysis) {
        Map<String, Object> insights = new HashMap<>();
        List<String> recommendations = new ArrayList<>();
        
        // File structure insights
        Map<String, Object> fileStructure = (Map<String, Object>) analysis.get("fileStructure");
        int totalFiles = (Integer) fileStructure.get("totalFiles");
        int codeFiles = (Integer) fileStructure.get("codeFiles");
        
        if (codeFiles > 0) {
            double codePercentage = (double) codeFiles / totalFiles * 100;
            insights.put("codePercentage", String.format("%.1f%%", codePercentage));
            
            if (codePercentage < 20) {
                recommendations.add("Consider adding more documentation and configuration files");
            } else if (codePercentage > 80) {
                recommendations.add("Consider adding more documentation for better maintainability");
            }
        }
        
        // Technology stack insights
        Map<String, Object> techStack = (Map<String, Object>) analysis.get("technologyStack");
        List<String> technologies = (List<String>) techStack.get("detectedTechnologies");
        
        if (technologies.size() > 3) {
            recommendations.add("Multiple technologies detected - consider simplifying the tech stack");
        }
        
        // Standards compliance insights
        Map<String, Object> standards = (Map<String, Object>) analysis.get("standards");
        int complianceScore = (Integer) standards.get("complianceScore");
        
        if (complianceScore < 50) {
            recommendations.add("Low standards compliance - add missing documentation files");
        }
        
        insights.put("recommendations", recommendations);
        insights.put("overallAssessment", getOverallAssessment(analysis));
        
        return insights;
    }

    /**
     * Helper methods
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1).toLowerCase() : "";
    }
    
    private double calculateComplexityScore(long totalFiles, long codeFiles) {
        if (totalFiles == 0) return 0.0;
        return (double) codeFiles / totalFiles * 100;
    }
    
    private String getComplexityLevel(double score) {
        if (score < 30) return "LOW";
        if (score < 60) return "MEDIUM";
        if (score < 80) return "HIGH";
        return "VERY_HIGH";
    }
    
    private String getComplianceLevel(int score) {
        if (score >= 80) return "EXCELLENT";
        if (score >= 60) return "GOOD";
        if (score >= 40) return "FAIR";
        if (score >= 20) return "POOR";
        return "CRITICAL";
    }
    
    private String getOverallAssessment(Map<String, Object> analysis) {
        // Simple assessment based on compliance and complexity
        Map<String, Object> standards = (Map<String, Object>) analysis.get("standards");
        Map<String, Object> complexity = (Map<String, Object>) analysis.get("complexity");
        
        int complianceScore = (Integer) standards.get("complianceScore");
        String complexityLevel = (String) complexity.get("complexityLevel");
        
        if (complianceScore >= 80 && !"VERY_HIGH".equals(complexityLevel)) {
            return "EXCELLENT";
        } else if (complianceScore >= 60 && !"VERY_HIGH".equals(complexityLevel)) {
            return "GOOD";
        } else if (complianceScore >= 40) {
            return "FAIR";
        } else {
            return "NEEDS_IMPROVEMENT";
        }
    }
}
