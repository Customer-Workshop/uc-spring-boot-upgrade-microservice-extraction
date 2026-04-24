package com.sure.budget.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateCategoryRequest(
        @NotBlank String name, String color, String icon, UUID parentId, String classification) {}
