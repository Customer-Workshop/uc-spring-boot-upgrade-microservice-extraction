package io.spring.articleservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record ArticleDto(
    String slug,
    String title,
    String description,
    String body,
    List<String> tagList,
    Instant createdAt,
    Instant updatedAt,
    boolean favorited,
    int favoritesCount,
    @JsonProperty("author") ProfileDto author) {}
