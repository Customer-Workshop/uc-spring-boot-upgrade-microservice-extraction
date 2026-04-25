package com.sure.budget.dto;


public record CategoryDto(
        String id, String familyId, String name, String color, String icon, String parentId, String classification) {}
