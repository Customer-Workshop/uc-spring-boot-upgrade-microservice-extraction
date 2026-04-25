package com.sure.transaction.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(
        @NotNull String accountId,
        String name,
        @NotNull LocalDate date,
        @NotNull BigDecimal amount,
        String currency,
        String notes,
        String categoryId,
        String merchantId,
        String kind,
        String nature) {}
