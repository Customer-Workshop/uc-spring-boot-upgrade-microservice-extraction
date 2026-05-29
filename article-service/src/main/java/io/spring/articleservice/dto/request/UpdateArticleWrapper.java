package io.spring.articleservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpdateArticleWrapper(@Valid @NotNull UpdateArticleRequest article) {}
