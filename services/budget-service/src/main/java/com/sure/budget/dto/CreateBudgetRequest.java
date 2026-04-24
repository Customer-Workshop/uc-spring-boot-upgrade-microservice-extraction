package com.sure.budget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateBudgetRequest(
        @NotBlank String name,
        @NotNull BigDecimal amount,
        String currency,
        UUID categoryId,
        String periodType,
        @NotNull LocalDate startDate,
        LocalDate endDate) {}
