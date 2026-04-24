package com.sure.budget.dto;

import java.util.UUID;

public record TagDto(UUID id, UUID familyId, String name, String color) {}
