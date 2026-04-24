package com.sure.transaction.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTransactionRequest(
        @NotNull UUID accountId,
        String name,
        @NotNull LocalDate date,
        @NotNull BigDecimal amount,
        String currency,
        String notes,
        UUID categoryId,
        UUID merchantId,
        String kind,
        String nature) {}
