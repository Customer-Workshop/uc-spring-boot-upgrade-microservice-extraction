package com.sure.account.controller;

import com.sure.account.dto.*;
import com.sure.account.service.AccountService;
import com.sure.common.dto.ApiResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountDto>>> getAccounts(Authentication auth) {
        UUID familyId = (UUID) auth.getCredentials();
        return ResponseEntity.ok(ApiResponse.ok(accountService.getAccounts(familyId)));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountDto>> getAccount(
            @PathVariable UUID accountId, Authentication auth) {
        UUID familyId = (UUID) auth.getCredentials();
        return ResponseEntity.ok(ApiResponse.ok(accountService.getAccount(accountId, familyId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountDto>> createAccount(
            @Valid @RequestBody CreateAccountRequest request, Authentication auth) {
        UUID familyId = (UUID) auth.getCredentials();
        AccountDto account = accountService.createAccount(familyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(account));
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountDto>> updateAccount(
            @PathVariable UUID accountId, @RequestBody UpdateAccountRequest request, Authentication auth) {
        UUID familyId = (UUID) auth.getCredentials();
        return ResponseEntity.ok(ApiResponse.ok(accountService.updateAccount(accountId, familyId, request)));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID accountId, Authentication auth) {
        UUID familyId = (UUID) auth.getCredentials();
        accountService.deleteAccount(accountId, familyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{accountId}/balances")
    public ResponseEntity<ApiResponse<List<BalanceDto>>> getBalanceHistory(
            @PathVariable UUID accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.getBalanceHistory(accountId, start, end)));
    }

    @GetMapping("/{accountId}/holdings")
    public ResponseEntity<ApiResponse<List<HoldingDto>>> getHoldings(@PathVariable UUID accountId) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.getHoldings(accountId)));
    }
}
