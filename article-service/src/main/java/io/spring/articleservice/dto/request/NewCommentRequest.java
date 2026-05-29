package io.spring.articleservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NewCommentRequest(@NotBlank(message = "can't be empty") String body) {}
