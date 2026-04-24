package com.sure.account.dto;

import java.math.BigDecimal;

public record UpdateAccountRequest(
        String name, String subtype, BigDecimal balance, String institutionName, Boolean excludedFromTotals) {}
