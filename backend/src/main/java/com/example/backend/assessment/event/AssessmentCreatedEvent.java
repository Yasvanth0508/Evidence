package com.example.backend.assessment.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class AssessmentCreatedEvent extends ApplicationEvent {

    private final UUID assessmentId;
    private final String repositoryUrl;
    private final String branchName;
    private final String backendRootDirectory;

    public AssessmentCreatedEvent(Object source,
                                  UUID assessmentId,
                                  String repositoryUrl,
                                  String branchName,
                                  String backendRootDirectory) {
        super(source);
        this.assessmentId = assessmentId;
        this.repositoryUrl = repositoryUrl;
        this.branchName = branchName;
        this.backendRootDirectory = backendRootDirectory;
    }
}
