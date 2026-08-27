package com.example.backend.assessment.entity;

import com.example.backend.common.entity.BaseEntity;
import com.example.backend.common.enums.TestType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "test_cases",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_assessment_testcase", columnNames = {"assessment_id", "test_case_number"})
    }
)
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @Column(name = "test_case_number", nullable = false)
    private Integer testCaseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false, length = 50)
    @Builder.Default
    private TestType testType = TestType.BUSINESS_LOGIC;

    @Column(name = "http_method", nullable = false, length = 20)
    private String httpMethod;

    @Column(name = "endpoint", nullable = false, length = 500)
    private String endpoint;

    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;

    @Column(name = "expected_status_code", nullable = false)
    private Integer expectedStatusCode;

    @Column(name = "expected_response", columnDefinition = "TEXT")
    private String expectedResponse;

    @Column(name = "assertions", columnDefinition = "TEXT", nullable = false)
    private String assertions;

    @Column(name = "weight", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal weight = BigDecimal.valueOf(1.00);
}
