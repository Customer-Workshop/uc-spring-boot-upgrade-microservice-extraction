package com.sure.account.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record HoldingDto(
        UUID id,
        UUID accountId,
        UUID securityId,
        String securityTicker,
        String securityName,
        LocalDate date,
        BigDecimal qty,
        BigDecimal price,
        BigDecimal amount,
        String currency,
        BigDecimal costBasis,
        String costBasisSource) {}
