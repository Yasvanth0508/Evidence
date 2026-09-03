package com.example.backend.candidate.controller;

import com.example.backend.candidate.dto.CandidateAssessmentItemResponse;
import com.example.backend.candidate.dto.CandidateSearchResponse;
import com.example.backend.candidate.service.CandidateLookupService;
import com.example.backend.candidate.service.CandidateAssessmentHistoryService;
import com.example.backend.common.dto.ApiResponse;
import com.example.backend.common.exception.BadRequestException;
import com.example.backend.common.util.UUIDUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
public class CandidateController {

    private final CandidateLookupService candidateLookupService;
    private final CandidateAssessmentHistoryService candidateAssessmentHistoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CandidateSearchResponse>>> getCandidates(
            @RequestParam(value = "query", required = false) String query) {
        log.info("Fetching candidates with query: {}", query);
        List<CandidateSearchResponse> response = candidateLookupService.getAllCandidates(query);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CandidateSearchResponse>> searchCandidateByEmail(
            @RequestParam("email") String email) {
        log.info("Searching for candidate by email: {}", email);
        CandidateSearchResponse response = candidateLookupService.searchCandidateByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{candidateId}/assessments")
    public ResponseEntity<ApiResponse<List<CandidateAssessmentItemResponse>>> getCandidateAssessments(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.example.backend.auth.security.UserPrincipal principal,
            @PathVariable String candidateId) {
        UUID candId = UUIDUtils.parseUuidOrNull(candidateId);
        if (candId == null) {
            throw new BadRequestException("Invalid candidate ID: must be a valid UUID");
        }
        if (principal == null) throw new com.example.backend.common.exception.UnauthorizedException("Not authenticated");
        UUID recruiterId = principal.getId();
        log.info("Fetching assessments for candidate ID: {} by recruiter ID: {}", candId, recruiterId);
        List<CandidateAssessmentItemResponse> response = candidateAssessmentHistoryService.getCandidateAssessmentsForRecruiter(recruiterId, candId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
