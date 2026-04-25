package com.sure.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EntryDto(
        String id,
        String accountId,
        String entryableType,
        String name,
        LocalDate date,
        BigDecimal amount,
        String currency,
        String notes,
        boolean excluded,
        boolean pending,
        boolean markedAsTransfer,
        TransactionDetail transaction,
        TradeDetail trade,
        LocalDateTime createdAt) {

    public record TransactionDetail(String id, String categoryId, String merchantId, String kind, String nature) {}

    public record TradeDetail(String id, String securityId, BigDecimal qty, BigDecimal price, String tradeType) {}
}
