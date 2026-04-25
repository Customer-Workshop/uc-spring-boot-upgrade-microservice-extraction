package com.sure.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountDto(
        String id,
        String familyId,
        String name,
        String accountType,
        String subtype,
        String currency,
        BigDecimal balance,
        String status,
        boolean active,
        String institutionName,
        String logoUrl,
        boolean excludedFromTotals,
        LocalDateTime createdAt) {}
