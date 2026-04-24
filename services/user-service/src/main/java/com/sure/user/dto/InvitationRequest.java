package com.sure.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InvitationRequest(@NotBlank @Email String email, String role) {}
