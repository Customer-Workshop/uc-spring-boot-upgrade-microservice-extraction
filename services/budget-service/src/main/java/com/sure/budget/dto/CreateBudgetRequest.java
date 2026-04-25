package com.sure.budget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateBudgetRequest(
        @NotBlank String name,
        @NotNull BigDecimal amount,
        String currency,
        String categoryId,
        String periodType,
        @NotNull LocalDate startDate,
        LocalDate endDate) {}
