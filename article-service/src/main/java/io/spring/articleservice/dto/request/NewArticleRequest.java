package io.spring.articleservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record NewArticleRequest(
    @NotBlank(message = "can't be empty") String title,
    @NotBlank(message = "can't be empty") String description,
    @NotBlank(message = "can't be empty") String body,
    List<String> tagList) {}
