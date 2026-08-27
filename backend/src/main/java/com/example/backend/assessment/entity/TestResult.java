package com.example.backend.assessment.entity;

import com.example.backend.common.enums.TestResultStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

@Entity
@Table(name = "test_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResult implements Persistable<UUID> {

    @Id
    @Column(name = "test_case_id", nullable = false)
    private UUID testCaseId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "test_case_id", nullable = false)
    private TestCase testCase;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private TestResultStatus status = TestResultStatus.PASSED;

    @Column(name = "actual_status_code")
    private Integer actualStatusCode;

    @Column(name = "actual_response", columnDefinition = "TEXT")
    private String actualResponse;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Transient
    @Builder.Default
    private boolean isNewEntity = true;

    public TestResult(TestCase testCase, TestResultStatus status,
                      Integer actualStatusCode, String actualResponse,
                      Long executionTimeMs, String failureReason) {
        this.testCase = testCase;
        this.testCaseId = testCase != null ? testCase.getId() : null;
        this.status = status != null ? status : TestResultStatus.PASSED;
        this.actualStatusCode = actualStatusCode;
        this.actualResponse = actualResponse;
        this.executionTimeMs = executionTimeMs;
        this.failureReason = failureReason;
        this.isNewEntity = true;
    }

    @PrePersist
    protected void onCreate() {
        if (this.testCaseId == null && testCase != null) {
            this.testCaseId = testCase.getId();
        }
    }

    @PostPersist
    @PostLoad
    protected void markNotNew() {
        this.isNewEntity = false;
    }

    @Override
    public UUID getId() {
        return testCaseId;
    }

    @Override
    public boolean isNew() {
        return isNewEntity;
    }
}
