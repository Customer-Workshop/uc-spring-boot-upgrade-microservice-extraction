package com.sure.transaction.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTradeRequest(
        @NotNull UUID accountId,
        String name,
        @NotNull LocalDate date,
        @NotNull BigDecimal amount,
        String currency,
        UUID securityId,
        @NotNull BigDecimal qty,
        @NotNull BigDecimal price,
        String tradeType) {}
