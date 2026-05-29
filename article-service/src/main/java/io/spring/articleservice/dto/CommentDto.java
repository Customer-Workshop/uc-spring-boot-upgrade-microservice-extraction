package io.spring.articleservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record CommentDto(
    String id,
    String body,
    Instant createdAt,
    Instant updatedAt,
    @JsonProperty("author") ProfileDto author) {}
