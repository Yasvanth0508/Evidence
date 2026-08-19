package com.example.backend.selectedcandidate.service;

import com.example.backend.assessment.entity.Assessment;
import com.example.backend.assessment.repository.AssessmentRepository;
import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.common.enums.Role;
import com.example.backend.common.exception.DuplicateResourceException;
import com.example.backend.common.exception.ResourceNotFoundException;
import com.example.backend.selectedcandidate.dto.SelectCandidateRequest;
import com.example.backend.selectedcandidate.dto.SelectedCandidateItemDto;
import com.example.backend.selectedcandidate.entity.SelectedCandidate;
import com.example.backend.selectedcandidate.repository.SelectedCandidateRepository;
import com.example.backend.workspace.entity.Workspace;
import com.example.backend.workspace.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SelectedCandidateService {

    private final SelectedCandidateRepository selectedCandidateRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AssessmentRepository assessmentRepository;

    public SelectedCandidateService(SelectedCandidateRepository selectedCandidateRepository,
                                    UserRepository userRepository,
                                    WorkspaceRepository workspaceRepository,
                                    AssessmentRepository assessmentRepository) {
        this.selectedCandidateRepository = selectedCandidateRepository;
        this.userRepository = userRepository;
        this.workspaceRepository = workspaceRepository;
        this.assessmentRepository = assessmentRepository;
    }

    private User getRecruiter() {
        return userRepository.findByEmail("recruiter@example.com")
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRole() == Role.RECRUITER)
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found", "USER_NOT_FOUND")));
    }

    public SelectedCandidateItemDto selectCandidate(SelectCandidateRequest request) {
        User recruiter = getRecruiter();

        User candidate = userRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found", "CANDIDATE_NOT_FOUND"));

        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found", "WORKSPACE_NOT_FOUND"));

        if (selectedCandidateRepository.existsByWorkspaceIdAndCandidateId(workspace.getId(), candidate.getId())) {
            throw new DuplicateResourceException("Candidate is already marked as selected in this workspace", "DUPLICATE_SELECTION");
        }

        Assessment assessment = null;
        if (request.getAssessmentId() != null) {
            assessment = assessmentRepository.findById(request.getAssessmentId()).orElse(null);
        }

        SelectedCandidate selectedCandidate = new SelectedCandidate(
                candidate,
                workspace,
                assessment,
                recruiter,
                request.getNotes(),
                Instant.now()
        );

        SelectedCandidate saved = selectedCandidateRepository.save(selectedCandidate);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SelectedCandidateItemDto> getSelectedCandidates(UUID workspaceId) {
        User recruiter = getRecruiter();
        List<SelectedCandidate> list;
        if (workspaceId != null) {
            list = selectedCandidateRepository.findAllByRecruiterIdAndWorkspaceId(recruiter.getId(), workspaceId);
        } else {
            list = selectedCandidateRepository.findAllByRecruiterId(recruiter.getId());
        }

        return list.stream().map(this::mapToDto).toList();
    }

    public void removeSelectedCandidate(UUID id) {
        SelectedCandidate sc = selectedCandidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Selected candidate record not found", "SELECTED_CANDIDATE_NOT_FOUND"));

        selectedCandidateRepository.delete(sc);
    }

    private SelectedCandidateItemDto mapToDto(SelectedCandidate sc) {
        BigDecimal score = null;
        UUID assessmentId = null;
        if (sc.getAssessment() != null) {
            assessmentId = sc.getAssessment().getId();
            score = sc.getAssessment().getScore() != null ? sc.getAssessment().getScore() : BigDecimal.valueOf(85.00);
        }

        return new SelectedCandidateItemDto(
                sc.getId(),
                sc.getCandidate().getId(),
                sc.getCandidate().getName(),
                sc.getCandidate().getEmail(),
                sc.getWorkspace().getId(),
                sc.getWorkspace().getName(),
                assessmentId,
                score,
                sc.getSelectionNotes(),
                sc.getSelectedAt()
        );
    }
}
