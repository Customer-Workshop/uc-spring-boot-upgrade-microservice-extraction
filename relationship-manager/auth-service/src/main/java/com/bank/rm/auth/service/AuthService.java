package com.bank.rm.auth.service;

import com.bank.rm.auth.domain.User;
import com.bank.rm.auth.dto.AuthDtos.*;
import com.bank.rm.auth.repository.UserRepository;
import com.bank.rm.common.exception.ResourceNotFoundException;
import com.bank.rm.common.exception.ValidationException;
import com.bank.rm.common.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ValidationException("Email already registered");
        }
        UUID userId = UUID.randomUUID();
        User user = new User(userId, request.email(), passwordEncoder.encode(request.password()),
                request.name(), request.phone(), null, false);
        userRepository.save(user);
        String token = jwtUtil.generateToken(userId, request.email());
        return new AuthResponse(userId, token, request.email(), request.name(),
                Instant.now().plus(24, ChronoUnit.HOURS));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ValidationException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(user.getId(), token, user.getEmail(), user.getName(),
                Instant.now().plus(24, ChronoUnit.HOURS));
    }

    public AnonymousSessionResponse createAnonymousSession() {
        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String anonEmail = "anon-" + sessionId + "@anonymous.local";
        User user = new User(userId, anonEmail, "", null, null, sessionId, true);
        userRepository.save(user);
        String token = jwtUtil.generateToken(userId, anonEmail);
        return new AnonymousSessionResponse(sessionId, token);
    }

    @Transactional
    public AuthResponse convertSession(ConvertSessionRequest request) {
        User user = userRepository.findByAnonymousSessionId(request.anonymousSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Anonymous session not found"));
        if (userRepository.existsByEmail(request.email())) {
            throw new ValidationException("Email already registered");
        }
        user.setAnonymous(false);
        user.setName(request.name());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), request.email());
        return new AuthResponse(user.getId(), token, request.email(), request.name(),
                Instant.now().plus(24, ChronoUnit.HOURS));
    }
}
