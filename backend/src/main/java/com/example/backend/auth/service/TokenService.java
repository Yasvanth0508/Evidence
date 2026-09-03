package com.example.backend.auth.service;

import com.example.backend.auth.entity.User;
import com.example.backend.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtTokenProvider jwtTokenProvider;

    public String generateToken(User user) {
        return jwtTokenProvider.generateToken(user);
    }
}
