package com.sure.budget.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTagRequest(@NotBlank String name, String color) {}
