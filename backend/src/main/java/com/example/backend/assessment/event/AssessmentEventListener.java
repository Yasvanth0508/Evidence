package com.example.backend.assessment.event;

import com.example.backend.assessment.application.AssessmentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssessmentEventListener {

    private final AssessmentProcessingService processingService;

    @Async("assessmentPipelineExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAssessmentCreated(AssessmentCreatedEvent event) {
        log.info("Assessment {} creation transaction committed. Starting background AI processing pipeline.",
                event.getAssessmentId());
        try {
            processingService.startProcessing(
                    event.getAssessmentId(),
                    event.getRepositoryUrl(),
                    event.getBranchName(),
                    event.getBackendRootDirectory()
            );
        } catch (Exception ex) {
            log.error("Unhandled exception initiating processing for assessment {}: {}",
                    event.getAssessmentId(), ex.getMessage(), ex);
        }
    }
}
