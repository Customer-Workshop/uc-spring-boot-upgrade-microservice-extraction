package com.sure.account.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank String name,
        @NotBlank String accountType,
        String subtype,
        String currency,
        BigDecimal balance,
        String institutionName,
        String institutionUrl) {}
