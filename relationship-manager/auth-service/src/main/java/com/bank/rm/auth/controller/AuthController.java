package com.bank.rm.auth.controller;

import com.bank.rm.auth.dto.AuthDtos.*;
import com.bank.rm.auth.service.AuthService;
import com.bank.rm.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/anonymous")
    public ResponseEntity<ApiResponse<AnonymousSessionResponse>> createAnonymousSession() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(authService.createAnonymousSession()));
    }

    @PostMapping("/convert")
    public ResponseEntity<ApiResponse<AuthResponse>> convertSession(@RequestBody ConvertSessionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.convertSession(request)));
    }
}
