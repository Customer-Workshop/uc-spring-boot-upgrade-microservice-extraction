package com.sure.user.dto;


public record AuthResponse(String token, UserResponse user) {

    public record UserResponse(
            String id, String email, String firstName, String lastName, String role, String familyId, String familyName) {}
}
