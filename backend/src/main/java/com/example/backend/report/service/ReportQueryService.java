package com.example.backend.report.service;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.common.enums.AssessmentStatus;
import com.example.backend.common.exception.ForbiddenException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.report.dto.ReportItemDto;
import com.example.backend.report.dto.ReportPageResponse;
import com.example.backend.workspace.entity.Workspace;
import com.example.backend.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportQueryService {

    private final AssessmentRepository assessmentRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ReportMapper reportMapper;

    @Transactional(readOnly = true)
    public ReportPageResponse getWorkspaceReports(UUID recruiterId, UUID workspaceId, AssessmentStatus status, int page, int size) {
        log.info("Fetching reports for workspace={}, recruiter={}, status={}, page={}, size={}",
                workspaceId, recruiterId, status, page, size);

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Assessment> assessmentPage;

        if (workspaceId != null) {
            Workspace workspace = workspaceRepository.findById(workspaceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Workspace not found: " + workspaceId));

            if (recruiterId != null && workspace.getRecruiter() != null && !workspace.getRecruiter().getId().equals(recruiterId)) {
                throw new ForbiddenException("Recruiter does not own this workspace");
            }

            if (status != null) {
                assessmentPage = assessmentRepository.findAllByWorkspaceIdAndStatus(workspaceId, status, pageable);
            } else {
                assessmentPage = assessmentRepository.findAllByWorkspaceId(workspaceId, pageable);
            }
        } else {
            if (recruiterId != null) {
                assessmentPage = assessmentRepository.findAllByRecruiterId(recruiterId, pageable);
            } else {
                assessmentPage = assessmentRepository.findAll(pageable);
            }
        }

        List<ReportItemDto> items = assessmentPage.getContent().stream()
                .map(reportMapper::toDto)
                .collect(Collectors.toList());

        return ReportPageResponse.builder()
                .content(items)
                .page(assessmentPage.getNumber())
                .size(assessmentPage.getSize())
                .totalElements(assessmentPage.getTotalElements())
                .totalPages(assessmentPage.getTotalPages())
                .build();
    }
}
