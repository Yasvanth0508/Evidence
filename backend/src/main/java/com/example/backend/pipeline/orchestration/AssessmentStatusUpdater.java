package com.example.backend.pipeline.orchestration;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.common.enums.AssessmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentStatusUpdater {

    private final AssessmentRepository assessmentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(UUID assessmentId, AssessmentStatus status) {
        if (assessmentId == null || status == null) {
            return;
        }
        try {
            Assessment assessment = assessmentRepository.findById(assessmentId).orElse(null);
            if (assessment != null) {
                assessment.setStatus(status);
                assessmentRepository.save(assessment);
                log.info("Committed status update for assessment {}: {}", assessmentId, status);
            } else {
                log.warn("Could not find assessment {} to update status to {}", assessmentId, status);
            }
        } catch (Exception ex) {
            log.error("Failed to commit status update for assessment {}: {}", assessmentId, ex.getMessage(), ex);
        }
    }
}
