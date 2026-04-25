package com.sure.account.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BalanceDto(String id, String accountId, LocalDate date, BigDecimal balance, String currency) {}
