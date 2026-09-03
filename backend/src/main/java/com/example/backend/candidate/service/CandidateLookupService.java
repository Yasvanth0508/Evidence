package com.example.backend.candidate.service;

import com.example.backend.auth.entity.User;
import com.example.backend.auth.repository.UserRepository;
import com.example.backend.candidate.dto.CandidateSearchResponse;
import com.example.backend.common.enums.Role;
import com.example.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidateLookupService {

    private final UserRepository userRepository;

    public List<CandidateSearchResponse> getAllCandidates(String query) {
        List<User> candidates;
        if (query != null && !query.trim().isEmpty()) {
            candidates = userRepository.searchCandidatesByRoleAndQuery(Role.CANDIDATE, query.trim());
        } else {
            candidates = userRepository.findAllByRole(Role.CANDIDATE);
        }

        return candidates.stream()
                .map(c -> CandidateSearchResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .email(c.getEmail())
                        .role(c.getRole())
                        .build())
                .collect(Collectors.toList());
    }

    public CandidateSearchResponse searchCandidateByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ResourceNotFoundException("Email query parameter is required", "CANDIDATE_NOT_FOUND");
        }

        User candidate = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with email: " + email, "CANDIDATE_NOT_FOUND"));

        if (candidate.getRole() != Role.CANDIDATE) {
            throw new ResourceNotFoundException("User is not registered as a candidate", "CANDIDATE_NOT_FOUND");
        }

        return CandidateSearchResponse.builder()
                .id(candidate.getId())
                .name(candidate.getName())
                .email(candidate.getEmail())
                .role(candidate.getRole())
                .build();
    }
}
