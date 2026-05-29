package io.spring.articleservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProfileDto(
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String id,
    String username,
    String bio,
    String image,
    boolean following) {}
