package com.example.backend.auth.service;

import com.example.backend.auth.domain.CurrentUser;
import com.example.backend.auth.security.UserPrincipal;
import com.example.backend.common.enums.Role;
import com.example.backend.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthorizationService {

    public CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("User is not authenticated");
        }
        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            return new CurrentUser(principal.getId(), principal.getEmail(), principal.getRole(), principal.getName());
        }
        throw new UnauthorizedException("Invalid authentication principal");
    }

    public CurrentUser requireAuthenticated() {
        return getCurrentUser();
    }

    public CurrentUser requireRecruiter() {
        CurrentUser user = getCurrentUser();
        if (user.role() != Role.RECRUITER) {
            throw new UnauthorizedException("Recruiter access required", "FORBIDDEN");
        }
        return user;
    }

    public CurrentUser requireCandidate() {
        CurrentUser user = getCurrentUser();
        if (user.role() != Role.CANDIDATE) {
            throw new UnauthorizedException("Candidate access required", "FORBIDDEN");
        }
        return user;
    }
}
