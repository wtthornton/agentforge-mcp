package com.agentforge.repository;

import com.agentforge.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.flyway.enabled=false",
	"spring.sql.init.mode=never",
	"logging.level.org.hibernate.SQL=OFF",
	"logging.level.org.hibernate.type.descriptor.sql.BasicBinder=OFF"
})
class RepositoryPerformanceTest {

	@Autowired UserRepository userRepository;
	@Autowired ProjectRepository projectRepository;
	@Autowired AnalysisRepository analysisRepository;
	@Autowired ComplianceViolationRepository complianceViolationRepository;

	private static long p95Millis(List<Long> durationsNanos) {
		if (durationsNanos.isEmpty()) return 0L;
		List<Long> copy = new ArrayList<>(durationsNanos);
		Collections.sort(copy);
		int index = (int) Math.ceil(copy.size() * 0.95) - 1;
		index = Math.max(0, Math.min(index, copy.size() - 1));
		return Duration.ofNanos(copy.get(index)).toMillis();
	}

	@Test
	@DisplayName("1.6.1: UserRepository CRUD P95 <100ms")
	void userRepositoryPerformance() {
		List<Long> durations = new ArrayList<>();
		for (int i = 0; i < 150; i++) {
			long start = System.nanoTime();
			User user = new User("user_" + UUID.randomUUID(),
					"user" + i + "@example.com",
					"hash" + i);
			user.setRole(UserRole.DEVELOPER);
			user = userRepository.save(user);
			assertNotNull(user.getId());

			user.setIsActive(i % 2 == 0);
			userRepository.save(user);
			Long userId = user.getId();
			var fetchedUserOpt = userRepository.findById(userId);
			assertTrue(fetchedUserOpt.isPresent());
			assertEquals(userId, fetchedUserOpt.get().getId());
			userRepository.delete(user);
			long end = System.nanoTime();
			durations.add(end - start);
		}
		long p95 = p95Millis(durations);
		assertTrue(p95 <= 100, "UserRepository P95 should be <=100ms but was " + p95 + "ms");
	}

	@Test
	@DisplayName("1.6.2: ProjectRepository CRUD P95 <100ms")
	void projectRepositoryPerformance() {
		List<Long> durations = new ArrayList<>();
		for (int i = 0; i < 150; i++) {
			long start = System.nanoTime();
			Project project = new Project("proj_" + UUID.randomUUID(), "desc" + i);
			project.setStatus(ProjectStatus.ACTIVE);
			project = projectRepository.save(project);
			assertNotNull(project.getId());

			project.setTechnologyStack("Java,Spring");
			projectRepository.save(project);
			Long projectId = project.getId();
			var fetchedProjectOpt = projectRepository.findById(projectId);
			assertTrue(fetchedProjectOpt.isPresent());
			assertEquals(projectId, fetchedProjectOpt.get().getId());
			projectRepository.delete(project);
			long end = System.nanoTime();
			durations.add(end - start);
		}
		long p95 = p95Millis(durations);
		assertTrue(p95 <= 100, "ProjectRepository P95 should be <=100ms but was " + p95 + "ms");
	}

	@Test
	@DisplayName("1.6.3: AnalysisRepository CRUD P95 <100ms")
	void analysisRepositoryPerformance() {
		// Need a user and project to link
		User user = userRepository.save(new User("perf_user_" + UUID.randomUUID(), "perf@example.com", "hash"));
		Project project = projectRepository.save(new Project("perf_proj_" + UUID.randomUUID(), "desc"));

		List<Long> durations = new ArrayList<>();
		for (int i = 0; i < 120; i++) {
			long start = System.nanoTime();
			Analysis analysis = new Analysis();
			analysis.setProject(project);
			analysis.setUser(user);
			analysis.setType(Analysis.AnalysisType.FULL);
			analysis.setStatus(Analysis.AnalysisStatus.PENDING);
			analysis.setStartTime(LocalDateTime.now());
			analysis = analysisRepository.save(analysis);
			assertNotNull(analysis.getId());

			analysis.completeAnalysis();
			analysisRepository.save(analysis);
			Long analysisId = analysis.getId();
			var fetchedAnalysisOpt = analysisRepository.findById(analysisId);
			assertTrue(fetchedAnalysisOpt.isPresent());
			assertEquals(analysisId, fetchedAnalysisOpt.get().getId());
			analysisRepository.delete(analysis);
			long end = System.nanoTime();
			durations.add(end - start);
		}
		long p95 = p95Millis(durations);
		assertTrue(p95 <= 100, "AnalysisRepository P95 should be <=100ms but was " + p95 + "ms");
	}

	@Test
	@DisplayName("1.6.4: ComplianceViolationRepository CRUD P95 <100ms")
	void complianceViolationRepositoryPerformance() {
		Project project = projectRepository.save(new Project("cv_proj_" + UUID.randomUUID(), "desc"));

		List<Long> durations = new ArrayList<>();
		for (int i = 0; i < 120; i++) {
			long start = System.nanoTime();
			ComplianceViolation v = new ComplianceViolation(project,
				"RULE-" + i,
				"Rule Name",
				"Security",
				ComplianceViolation.ViolationSeverity.MEDIUM,
				"Message");
			v.setFilePath("src/File.java");
			v.setLineNumber(10 + i);
			v = complianceViolationRepository.save(v);
			assertNotNull(v.getId());

			v.setStatus(ComplianceViolation.ViolationStatus.IN_PROGRESS);
			complianceViolationRepository.save(v);
			Long vId = v.getId();
			var fetchedViolationOpt = complianceViolationRepository.findById(vId);
			assertTrue(fetchedViolationOpt.isPresent());
			assertEquals(vId, fetchedViolationOpt.get().getId());
			complianceViolationRepository.delete(v);
			long end = System.nanoTime();
			durations.add(end - start);
		}
		long p95 = p95Millis(durations);
		assertTrue(p95 <= 100, "ComplianceViolationRepository P95 should be <=100ms but was " + p95 + "ms");
	}
}
