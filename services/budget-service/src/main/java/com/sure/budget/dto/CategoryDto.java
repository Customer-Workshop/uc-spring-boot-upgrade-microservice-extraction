package com.sure.budget.dto;

import java.util.UUID;

public record CategoryDto(
        UUID id, UUID familyId, String name, String color, String icon, UUID parentId, String classification) {}
