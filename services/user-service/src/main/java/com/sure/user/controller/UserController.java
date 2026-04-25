package com.sure.user.controller;

import com.sure.common.dto.ApiResponse;
import com.sure.user.dto.AuthResponse;
import com.sure.user.dto.UpdateUserRequest;
import com.sure.user.service.UserService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/family")
    public ResponseEntity<ApiResponse<List<AuthResponse.UserResponse>>> getFamilyMembers(Authentication auth) {
        String familyId = (String) auth.getCredentials();
        List<AuthResponse.UserResponse> members = userService.getFamilyMembers(familyId);
        return ResponseEntity.ok(ApiResponse.ok(members));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse.UserResponse>> updateCurrentUser(
            Authentication auth, @RequestBody UpdateUserRequest request) {
        String userId = (String) auth.getPrincipal();
        AuthResponse.UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
