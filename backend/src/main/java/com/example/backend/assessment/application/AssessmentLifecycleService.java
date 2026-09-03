package com.example.backend.assessment.application;

import com.example.backend.assessment.dto.AssessmentStatusResponse;
import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.common.exception.ForbiddenException;
import com.example.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentLifecycleService {

    private final AssessmentRepository assessmentRepository;

    @Transactional(readOnly = true)
    public AssessmentStatusResponse getAssessmentStatus(UUID recruiterId, UUID candidateId, UUID assessmentId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + assessmentId, "ASSESSMENT_NOT_FOUND"));

        if (candidateId != null && !assessment.getCandidate().getId().equals(candidateId)) {
            throw new ForbiddenException("You do not have permission to view this assessment status.", "FORBIDDEN");
        } else if (recruiterId != null && !assessment.getWorkspace().getRecruiter().getId().equals(recruiterId)) {
            throw new ForbiddenException("You do not have permission to view this assessment status.", "FORBIDDEN");
        }

        return AssessmentStatusResponse.builder()
                .assessmentId(assessment.getId())
                .status(assessment.getStatus())
                .scheduledStartAt(assessment.getScheduledStartAt())
                .scheduledEndAt(assessment.getScheduledEndAt())
                .build();
    }
}
