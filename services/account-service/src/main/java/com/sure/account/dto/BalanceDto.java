package com.sure.account.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BalanceDto(UUID id, UUID accountId, LocalDate date, BigDecimal balance, String currency) {}
