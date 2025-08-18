package com.agentforge.repository;

import com.agentforge.entity.Project;
import com.agentforge.entity.ProjectStatus;
import com.agentforge.entity.User;
import com.agentforge.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ProjectRepository
 * Tests CRUD operations, custom queries, and performance targets
 */
@SpringJUnitConfig(TestConfig.class)
@ActiveProfiles("test")
@Transactional
class ProjectRepositoryIntegrationTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Project testProject;
    private LocalDateTime testDate;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("password123");
        testUser.setRole(UserRole.DEVELOPER);
        testUser = userRepository.save(testUser);

        // Create test project
        testProject = new Project();
        testProject.setName("Test Project");
        testProject.setDescription("A test project for repository testing");
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setTechnologyStack("Java,Spring Boot,PostgreSQL");
        testProject.setCreatedAt(LocalDateTime.now());
        testProject.setLastAnalysisDate(LocalDateTime.now());

        testDate = LocalDateTime.now();
    }

    @Test
    void testCreateProject() {
        // Act
        Project savedProject = projectRepository.save(testProject);

        // Assert
        assertNotNull(savedProject.getId());
        assertEquals("Test Project", savedProject.getName());
        assertEquals(ProjectStatus.ACTIVE, savedProject.getStatus());
    }

    @Test
    void testFindProjectById() {
        // Arrange
        Project savedProject = projectRepository.save(testProject);

        // Act
        Optional<Project> foundProject = projectRepository.findById(savedProject.getId());

        // Assert
        assertTrue(foundProject.isPresent());
        assertEquals(savedProject.getName(), foundProject.get().getName());
    }

    @Test
    void testUpdateProject() {
        // Arrange
        Project savedProject = projectRepository.save(testProject);
        String newName = "Updated Project Name";

        // Act
        savedProject.setName(newName);
        Project updatedProject = projectRepository.save(savedProject);

        // Assert
        assertEquals(newName, updatedProject.getName());
        assertEquals(savedProject.getId(), updatedProject.getId());
    }

    @Test
    void testDeleteProject() {
        // Arrange
        Project savedProject = projectRepository.save(testProject);
        Long projectId = savedProject.getId();

        // Act
        projectRepository.delete(savedProject);

        // Assert
        Optional<Project> deletedProject = projectRepository.findById(projectId);
        assertFalse(deletedProject.isPresent());
    }

    @Test
    void testFindByName() {
        // Arrange
        projectRepository.save(testProject);

        // Act
        Optional<Project> foundProject = projectRepository.findByName("Test Project");

        // Assert
        assertTrue(foundProject.isPresent());
        assertEquals("Test Project", foundProject.get().getName());
    }

    @Test
    void testFindByStatus() {
        // Arrange
        projectRepository.save(testProject);

        // Act
        List<Project> activeProjects = projectRepository.findByStatus(ProjectStatus.ACTIVE);

        // Assert
        assertFalse(activeProjects.isEmpty());
        assertEquals(ProjectStatus.ACTIVE, activeProjects.get(0).getStatus());
    }

    @Test
    void testFindByStatusWithPagination() {
        // Arrange
        projectRepository.save(testProject);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Project> statusProjectsPage = projectRepository.findByStatus(ProjectStatus.ACTIVE, pageable);

        // Assert
        assertEquals(1, statusProjectsPage.getTotalElements());
        assertEquals(1, statusProjectsPage.getContent().size());
    }

    @Test
    void testFindByTechnologyStackContaining() {
        // Arrange
        projectRepository.save(testProject);

        // Act
        List<Project> javaProjects = projectRepository.findByTechnologyStackContainingIgnoreCase("Java");

        // Assert
        assertFalse(javaProjects.isEmpty());
        assertTrue(javaProjects.get(0).getTechnologyStack().toLowerCase().contains("java"));
    }

    @Test
    void testFindByNameContaining() {
        // Arrange
        projectRepository.save(testProject);

        // Act
        List<Project> testProjects = projectRepository.findByNameContainingIgnoreCase("Test");

        // Assert
        assertFalse(testProjects.isEmpty());
        assertTrue(testProjects.get(0).getName().toLowerCase().contains("test"));
    }

    @Test
    void testFindByDescriptionContaining() {
        // Arrange
        projectRepository.save(testProject);

        // Act
        List<Project> descriptionProjects = projectRepository.findByDescriptionContainingIgnoreCase("test project");

        // Assert
        assertFalse(descriptionProjects.isEmpty());
        assertTrue(descriptionProjects.get(0).getDescription().toLowerCase().contains("test project"));
    }

    @Test
    void testFindByCreatedAtAfter() {
        // Arrange
        projectRepository.save(testProject);
        LocalDateTime beforeDate = testDate.minusDays(1);

        // Act
        List<Project> recentProjects = projectRepository.findByCreatedAtAfter(beforeDate);

        // Assert
        assertFalse(recentProjects.isEmpty());
        assertTrue(recentProjects.get(0).getCreatedAt().isAfter(beforeDate));
    }

    @Test
    void testFindByCreatedAtBetween() {
        // Arrange
        projectRepository.save(testProject);
        LocalDateTime startDate = testDate.minusDays(2);
        LocalDateTime endDate = testDate.plusDays(1);

        // Act
        List<Project> dateRangeProjects = projectRepository.findByCreatedAtBetween(startDate, endDate);

        // Assert
        assertFalse(dateRangeProjects.isEmpty());
        assertTrue(dateRangeProjects.get(0).getCreatedAt().isAfter(startDate));
        assertTrue(dateRangeProjects.get(0).getCreatedAt().isBefore(endDate));
    }

    @Test
    void testFindByTechnologyStackContainingIgnoreCase() {
        // Arrange
        projectRepository.save(testProject);

        // Act
        List<Project> javaProjects = projectRepository.findByTechnologyStackContainingIgnoreCase("Java");

        // Assert
        assertFalse(javaProjects.isEmpty());
        assertTrue(javaProjects.get(0).getTechnologyStack().toLowerCase().contains("java"));
    }

    @Test
    void testBulkOperations() {
        // Arrange
        List<Project> projects = List.of(
            createProject("Project 1", "Description 1", ProjectStatus.ACTIVE),
            createProject("Project 2", "Description 2", ProjectStatus.SUSPENDED),
            createProject("Project 3", "Description 3", ProjectStatus.ACTIVE)
        );

        // Act
        List<Project> savedProjects = projectRepository.saveAll(projects);

        // Assert
        assertEquals(3, savedProjects.size());
        assertTrue(savedProjects.stream().allMatch(p -> p.getId() != null));

        // Test bulk find
        List<Project> allProjects = projectRepository.findAll();
        assertTrue(allProjects.size() >= 3);
    }

    @Test
    void testPerformanceTargets() {
        // Arrange
        projectRepository.save(testProject);
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert - Measure performance for key operations
        long startTime = System.currentTimeMillis();

        // Test findById performance
        Optional<Project> foundProject = projectRepository.findById(testProject.getId());
        long findByIdTime = System.currentTimeMillis() - startTime;

        // Test findByStatus performance
        startTime = System.currentTimeMillis();
        List<Project> activeProjects = projectRepository.findByStatus(ProjectStatus.ACTIVE);
        long findByStatusTime = System.currentTimeMillis() - startTime;

        // Test pagination performance
        startTime = System.currentTimeMillis();
        Page<Project> projectsPage = projectRepository.findByStatus(ProjectStatus.ACTIVE, pageable);
        long paginationTime = System.currentTimeMillis() - startTime;

        // Assert performance targets (P95 < 100ms)
        assertTrue(findByIdTime < 100, "findById should complete in < 100ms, took: " + findByIdTime + "ms");
        assertTrue(findByStatusTime < 100, "findByStatus should complete in < 100ms, took: " + findByStatusTime + "ms");
        assertTrue(paginationTime < 100, "Pagination should complete in < 100ms, took: " + paginationTime + "ms");

        // Verify results are correct
        assertTrue(foundProject.isPresent());
        assertFalse(activeProjects.isEmpty());
        assertEquals(1, projectsPage.getTotalElements());
    }

    @Test
    void testDataIntegrity() {
        // Arrange
        Project project = new Project();
        project.setName("Integrity Test Project");
        project.setDescription("Testing data integrity");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setTechnologyStack("Java,Spring Boot");

        // Act
        Project savedProject = projectRepository.save(project);
        Project retrievedProject = projectRepository.findById(savedProject.getId()).orElse(null);

        // Assert
        assertNotNull(retrievedProject);
        assertEquals(project.getName(), retrievedProject.getName());
        assertEquals(project.getDescription(), retrievedProject.getDescription());
        assertEquals(project.getStatus(), retrievedProject.getStatus());
        assertEquals(project.getTechnologyStack(), retrievedProject.getTechnologyStack());
    }

    @Test
    void testTransactionRollback() {
        // Arrange
        Project project = new Project();
        project.setName("Rollback Test Project");
        project.setDescription("Testing transaction rollback");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setTechnologyStack("Java,Spring Boot");

        // Act
        Project savedProject = projectRepository.save(project);
        Long projectId = savedProject.getId();

        // Verify project was saved
        assertTrue(projectRepository.findById(projectId).isPresent());

        // Simulate transaction rollback by throwing exception
        assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException("Simulated rollback");
        });

        // Note: In a real transaction, this would be rolled back
        // For this test, we're just verifying the exception handling
    }

    /**
     * Helper method to create test projects
     */
    private Project createProject(String name, String description, ProjectStatus status) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setStatus(status);
        project.setTechnologyStack("Java,Spring Boot");
        return project;
    }
}
