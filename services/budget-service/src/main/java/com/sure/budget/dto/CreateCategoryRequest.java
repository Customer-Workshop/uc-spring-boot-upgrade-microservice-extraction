package com.sure.budget.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank String name, String color, String icon, String parentId, String classification) {}
