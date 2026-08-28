package com.example.backend.assessment.repository;

import com.example.backend.assessment.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestResultRepository extends JpaRepository<TestResult, UUID> {
    Optional<TestResult> findByTestCaseId(UUID testCaseId);

    @Query("SELECT tr FROM TestResult tr WHERE tr.testCase.assessment.id = :assessmentId")
    List<TestResult> findAllByAssessmentId(@Param("assessmentId") UUID assessmentId);
}
