package io.spring.articleservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record ProfileDto(
    @JsonIgnore String id, String username, String bio, String image, boolean following) {}
