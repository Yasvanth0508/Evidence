package com.example.backend.execution.service;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.common.enums.ApplicationStatus;
import com.example.backend.common.enums.BuildStatus;
import com.example.backend.common.enums.ContainerStatus;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.execution.dto.ExecutionLogsResponse;
import com.example.backend.execution.dto.ExecutionResponse;
import com.example.backend.execution.dto.ExecutionStatusResponse;
import com.example.backend.execution.entity.Execution;
import com.example.backend.execution.repository.ExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class ExecutionService {

    private final ExecutionRepository executionRepository;
    private final AssessmentRepository assessmentRepository;

    public ExecutionService(ExecutionRepository executionRepository,
                            AssessmentRepository assessmentRepository) {
        this.executionRepository = executionRepository;
        this.assessmentRepository = assessmentRepository;
    }

    public ExecutionResponse runApplication(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        Instant now = Instant.now();
        String dummyContainerId = "cont-" + UUID.randomUUID().toString().substring(0, 8);

        Execution execution = new Execution(
                assessment,
                dummyContainerId,
                BuildStatus.SUCCESS,
                ContainerStatus.RUNNING,
                ApplicationStatus.STARTED,
                now,
                null
        );

        Execution saved = executionRepository.save(execution);

        return new ExecutionResponse(
                saved.getId(),
                saved.getBuildStatus(),
                saved.getContainerStatus(),
                saved.getApplicationStatus(),
                saved.getStartedAt()
        );
    }

    public ExecutionResponse stopApplication(UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        Instant now = Instant.now();

        Execution execution = executionRepository.findTopByAssessmentIdOrderByCreatedAtDesc(assessment.getId())
                .orElseGet(() -> new Execution(
                        assessment,
                        "cont-stopped",
                        BuildStatus.SUCCESS,
                        ContainerStatus.STOPPED,
                        ApplicationStatus.STARTED,
                        now.minusSeconds(60),
                        now
                ));

        execution.setContainerStatus(ContainerStatus.STOPPED);
        execution.setStoppedAt(now);
        Execution saved = executionRepository.save(execution);

        return new ExecutionResponse(
                saved.getId(),
                saved.getBuildStatus(),
                saved.getContainerStatus(),
                saved.getApplicationStatus(),
                saved.getStartedAt()
        );
    }

    @Transactional(readOnly = true)
    public ExecutionStatusResponse getExecutionStatus(UUID assessmentId) {
        assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        return executionRepository.findTopByAssessmentIdOrderByCreatedAtDesc(assessmentId)
                .map(e -> new ExecutionStatusResponse(
                        e.getBuildStatus(),
                        e.getContainerStatus(),
                        e.getApplicationStatus(),
                        e.getStartedAt(),
                        e.getStoppedAt()
                ))
                .orElseGet(() -> new ExecutionStatusResponse(
                        BuildStatus.SUCCESS,
                        ContainerStatus.RUNNING,
                        ApplicationStatus.STARTED,
                        Instant.now().minusSeconds(120),
                        null
                ));
    }

    @Transactional(readOnly = true)
    public ExecutionLogsResponse getExecutionLogs(UUID assessmentId) {
        assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found", "ASSESSMENT_NOT_FOUND"));

        String mockLogs = """
                  .   ____          _            __ _ _
                 /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\
                ( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\
                 \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )
                  '  |____| .__|_| |_|_| |_\\__, | / / / /
                 =========|_|==============|___/=/_/_/_/
                 :: Spring Boot ::                (v4.1.0)

                2026-08-19 10:30:01.102  INFO 1 --- [main] c.e.demo.DemoApplication      : Starting DemoApplication using Java 21
                2026-08-19 10:30:01.108  INFO 1 --- [main] c.e.demo.DemoApplication      : No active profile set, falling back to 1 default profile: "default"
                2026-08-19 10:30:02.450  INFO 1 --- [main] o.s.b.w.e.tomcat.TomcatWebServer: Tomcat initialized with port 8080 (http)
                2026-08-19 10:30:02.465  INFO 1 --- [main] o.a.c.c.C.[Tomcat].[localhost].[/] : Initializing Spring embedded Database/JPA repository
                2026-08-19 10:30:03.112  INFO 1 --- [main] o.s.d.j.r.c.RepositoryConfiguration: Bootstrapping Spring Data JPA repositories in DEFAULT mode.
                2026-08-19 10:30:03.421  INFO 1 --- [main] o.s.d.j.r.c.RepositoryConfiguration: Finished Spring Data repository scanning in 309 ms.
                2026-08-19 10:30:04.810  INFO 1 --- [main] o.s.b.w.e.tomcat.TomcatWebServer: Tomcat started on port 8080 (http) with context path '/'
                2026-08-19 10:30:04.825  INFO 1 --- [main] c.e.demo.DemoApplication      : Started DemoApplication in 3.723 seconds (process running for 4.102)
                """;

        return new ExecutionLogsResponse(mockLogs);
    }
}
