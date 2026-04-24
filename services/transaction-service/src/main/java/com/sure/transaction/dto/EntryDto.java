package com.sure.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record EntryDto(
        UUID id,
        UUID accountId,
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

    public record TransactionDetail(UUID id, UUID categoryId, UUID merchantId, String kind, String nature) {}

    public record TradeDetail(UUID id, UUID securityId, BigDecimal qty, BigDecimal price, String tradeType) {}
}
