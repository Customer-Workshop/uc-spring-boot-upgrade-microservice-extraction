package com.sure.transaction.controller;

import com.sure.common.dto.ApiResponse;
import com.sure.common.dto.PageResponse;
import com.sure.transaction.dto.*;
import com.sure.transaction.service.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/entries")
    public ResponseEntity<ApiResponse<PageResponse<EntryDto>>> getEntries(
            @RequestParam List<String> accountIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        Page<EntryDto> result = transactionService.getEntries(accountIds, PageRequest.of(page, size));
        PageResponse<EntryDto> pageResponse = new PageResponse<>(
                result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(),
                result.getTotalPages());
        return ResponseEntity.ok(ApiResponse.ok(pageResponse));
    }

    @GetMapping("/entries/{entryId}")
    public ResponseEntity<ApiResponse<EntryDto>> getEntry(@PathVariable String entryId) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.getEntry(entryId)));
    }

    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<EntryDto>> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        EntryDto entry = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(entry));
    }

    @PutMapping("/entries/{entryId}")
    public ResponseEntity<ApiResponse<EntryDto>> updateTransaction(
            @PathVariable String entryId, @RequestBody UpdateTransactionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(transactionService.updateTransaction(entryId, request)));
    }

    @DeleteMapping("/entries/{entryId}")
    public ResponseEntity<Void> deleteEntry(@PathVariable String entryId) {
        transactionService.deleteEntry(entryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/trades")
    public ResponseEntity<ApiResponse<EntryDto>> createTrade(@Valid @RequestBody CreateTradeRequest request) {
        EntryDto entry = transactionService.createTrade(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(entry));
    }
}
