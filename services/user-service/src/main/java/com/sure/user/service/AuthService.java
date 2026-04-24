package com.sure.user.service;

import com.sure.common.security.JwtTokenProvider;
import com.sure.user.dto.AuthResponse;
import com.sure.user.dto.LoginRequest;
import com.sure.user.dto.RegisterRequest;
import com.sure.user.entity.Family;
import com.sure.user.entity.User;
import com.sure.user.repository.FamilyRepository;
import com.sure.user.repository.UserRepository;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserRepository userRepository,
            FamilyRepository familyRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.familyRepository = familyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Family family = Family.builder()
                .name(request.familyName() != null ? request.familyName() : request.firstName() + "'s Family")
                .currency(request.currency() != null ? request.currency() : "USD")
                .build();
        family = familyRepository.save(family);

        User user = User.builder()
                .email(request.email())
                .passwordDigest(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .family(family)
                .role("admin")
                .build();
        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole(), family.getId());
        return new AuthResponse(token, toUserResponse(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository
                .findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordDigest())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(
                user.getId(), user.getEmail(), user.getRole(), user.getFamily().getId());
        return new AuthResponse(token, toUserResponse(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        String token = jwtTokenProvider.generateToken(
                user.getId(), user.getEmail(), user.getRole(), user.getFamily().getId());
        return new AuthResponse(token, toUserResponse(user));
    }

    private AuthResponse.UserResponse toUserResponse(User user) {
        return new AuthResponse.UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getFamily().getId(),
                user.getFamily().getName());
    }
}
