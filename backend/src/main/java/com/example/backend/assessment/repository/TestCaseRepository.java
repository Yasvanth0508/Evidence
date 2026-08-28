package com.example.backend.assessment.repository;

import com.example.backend.assessment.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TestCaseRepository extends JpaRepository<TestCase, UUID> {
    List<TestCase> findAllByAssessmentIdOrderByTestCaseNumberAsc(UUID assessmentId);
    long countByAssessmentId(UUID assessmentId);
    void deleteByAssessmentId(UUID assessmentId);
}
