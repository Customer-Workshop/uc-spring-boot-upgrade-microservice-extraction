package com.bank.rm.auth.dto;

import java.time.Instant;
import java.util.UUID;

public class AuthDtos {

    public record RegisterRequest(String email, String password, String name, String phone) {}

    public record LoginRequest(String email, String password) {}

    public record ConvertSessionRequest(UUID anonymousSessionId, String email, String password, String name) {}

    public record AuthResponse(UUID userId, String token, String email, String name, Instant expiresAt) {}

    public record AnonymousSessionResponse(UUID sessionId, String token) {}

    private AuthDtos() {}
}
