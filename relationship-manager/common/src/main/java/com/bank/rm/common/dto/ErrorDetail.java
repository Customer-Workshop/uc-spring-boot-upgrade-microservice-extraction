package com.bank.rm.common.dto;

import java.util.List;

public record ErrorDetail(
    String code,
    String message,
    List<FieldError> details,
    String traceId
) {}
