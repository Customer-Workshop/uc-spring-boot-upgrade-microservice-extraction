package com.sure.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateTransactionRequest(
        String name,
        LocalDate date,
        BigDecimal amount,
        String notes,
        UUID categoryId,
        UUID merchantId,
        Boolean excluded) {}
