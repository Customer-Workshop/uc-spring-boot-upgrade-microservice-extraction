package com.sure.user.dto;

import java.util.UUID;

public record AuthResponse(String token, UserResponse user) {

    public record UserResponse(
            UUID id, String email, String firstName, String lastName, String role, UUID familyId, String familyName) {}
}
