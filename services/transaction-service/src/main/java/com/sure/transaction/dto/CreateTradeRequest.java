package com.sure.transaction.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTradeRequest(
        @NotNull String accountId,
        String name,
        @NotNull LocalDate date,
        @NotNull BigDecimal amount,
        String currency,
        String securityId,
        @NotNull BigDecimal qty,
        @NotNull BigDecimal price,
        String tradeType) {}
