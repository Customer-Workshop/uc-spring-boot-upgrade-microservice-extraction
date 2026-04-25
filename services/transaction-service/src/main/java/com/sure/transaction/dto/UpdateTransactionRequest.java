package com.sure.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateTransactionRequest(
        String name,
        LocalDate date,
        BigDecimal amount,
        String notes,
        String categoryId,
        String merchantId,
        Boolean excluded) {}
