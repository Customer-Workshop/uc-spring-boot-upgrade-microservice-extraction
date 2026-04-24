package com.sure.user.service;

import com.sure.user.dto.AuthResponse;
import com.sure.user.dto.UpdateUserRequest;
import com.sure.user.entity.User;
import com.sure.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AuthResponse.UserResponse> getFamilyMembers(UUID familyId) {
        return userRepository.findAll().stream()
                .filter(u -> u.getFamily().getId().equals(familyId))
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AuthResponse.UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.profileImage() != null) {
            user.setProfileImage(request.profileImage());
        }

        user = userRepository.save(user);
        return toUserResponse(user);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
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
