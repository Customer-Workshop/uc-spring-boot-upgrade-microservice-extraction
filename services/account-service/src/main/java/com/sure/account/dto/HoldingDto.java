package com.sure.account.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HoldingDto(
        String id,
        String accountId,
        String securityId,
        String securityTicker,
        String securityName,
        LocalDate date,
        BigDecimal qty,
        BigDecimal price,
        BigDecimal amount,
        String currency,
        BigDecimal costBasis,
        String costBasisSource) {}
