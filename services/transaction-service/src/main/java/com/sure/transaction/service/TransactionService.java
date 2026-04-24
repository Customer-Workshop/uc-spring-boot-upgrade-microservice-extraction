package com.sure.transaction.service;

import com.sure.transaction.dto.*;
import com.sure.transaction.entity.Entry;
import com.sure.transaction.entity.Trade;
import com.sure.transaction.entity.Transaction;
import com.sure.transaction.repository.EntryRepository;
import com.sure.transaction.repository.TradeRepository;
import com.sure.transaction.repository.TransactionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final EntryRepository entryRepository;
    private final TransactionRepository transactionRepository;
    private final TradeRepository tradeRepository;

    public TransactionService(
            EntryRepository entryRepository,
            TransactionRepository transactionRepository,
            TradeRepository tradeRepository) {
        this.entryRepository = entryRepository;
        this.transactionRepository = transactionRepository;
        this.tradeRepository = tradeRepository;
    }

    @Transactional(readOnly = true)
    public Page<EntryDto> getEntries(List<UUID> accountIds, Pageable pageable) {
        return entryRepository
                .findByAccountIdInAndExcludedFalseOrderByDateDesc(accountIds, pageable)
                .map(this::toEntryDto);
    }

    @Transactional(readOnly = true)
    public EntryDto getEntry(UUID entryId) {
        Entry entry =
                entryRepository.findById(entryId).orElseThrow(() -> new IllegalArgumentException("Entry not found"));
        return toEntryDto(entry);
    }

    @Transactional
    public EntryDto createTransaction(CreateTransactionRequest request) {
        Entry entry = Entry.builder()
                .accountId(request.accountId())
                .entryableType("Transaction")
                .name(request.name())
                .date(request.date())
                .amount(request.amount())
                .currency(request.currency() != null ? request.currency() : "USD")
                .notes(request.notes())
                .build();
        entry = entryRepository.save(entry);

        Transaction transaction = Transaction.builder()
                .entry(entry)
                .categoryId(request.categoryId())
                .merchantId(request.merchantId())
                .kind(request.kind() != null ? request.kind() : "standard")
                .nature(request.nature())
                .build();
        transactionRepository.save(transaction);

        return toEntryDto(entry);
    }

    @Transactional
    public EntryDto updateTransaction(UUID entryId, UpdateTransactionRequest request) {
        Entry entry =
                entryRepository.findById(entryId).orElseThrow(() -> new IllegalArgumentException("Entry not found"));

        if (request.name() != null) entry.setName(request.name());
        if (request.date() != null) entry.setDate(request.date());
        if (request.amount() != null) entry.setAmount(request.amount());
        if (request.notes() != null) entry.setNotes(request.notes());
        if (request.excluded() != null) entry.setExcluded(request.excluded());
        entry = entryRepository.save(entry);

        if ("Transaction".equals(entry.getEntryableType())) {
            transactionRepository.findByEntryId(entryId).ifPresent(txn -> {
                if (request.categoryId() != null) txn.setCategoryId(request.categoryId());
                if (request.merchantId() != null) txn.setMerchantId(request.merchantId());
                transactionRepository.save(txn);
            });
        }

        return toEntryDto(entry);
    }

    @Transactional
    public void deleteEntry(UUID entryId) {
        entryRepository.deleteById(entryId);
    }

    @Transactional
    public EntryDto createTrade(CreateTradeRequest request) {
        Entry entry = Entry.builder()
                .accountId(request.accountId())
                .entryableType("Trade")
                .name(request.name())
                .date(request.date())
                .amount(request.amount())
                .currency(request.currency() != null ? request.currency() : "USD")
                .build();
        entry = entryRepository.save(entry);

        Trade trade = Trade.builder()
                .entry(entry)
                .securityId(request.securityId())
                .qty(request.qty())
                .price(request.price())
                .tradeType(request.tradeType() != null ? request.tradeType() : "buy")
                .currency(request.currency() != null ? request.currency() : "USD")
                .build();
        tradeRepository.save(trade);

        return toEntryDto(entry);
    }

    private EntryDto toEntryDto(Entry entry) {
        EntryDto.TransactionDetail txnDetail = null;
        EntryDto.TradeDetail tradeDetail = null;

        if ("Transaction".equals(entry.getEntryableType())) {
            transactionRepository.findByEntryId(entry.getId()).ifPresent(txn -> {});
            txnDetail = transactionRepository
                    .findByEntryId(entry.getId())
                    .map(txn -> new EntryDto.TransactionDetail(
                            txn.getId(), txn.getCategoryId(), txn.getMerchantId(), txn.getKind(), txn.getNature()))
                    .orElse(null);
        } else if ("Trade".equals(entry.getEntryableType())) {
            tradeDetail = tradeRepository
                    .findByEntryId(entry.getId())
                    .map(t -> new EntryDto.TradeDetail(
                            t.getId(), t.getSecurityId(), t.getQty(), t.getPrice(), t.getTradeType()))
                    .orElse(null);
        }

        return new EntryDto(
                entry.getId(),
                entry.getAccountId(),
                entry.getEntryableType(),
                entry.getName(),
                entry.getDate(),
                entry.getAmount(),
                entry.getCurrency(),
                entry.getNotes(),
                entry.isExcluded(),
                entry.isPending(),
                entry.isMarkedAsTransfer(),
                txnDetail,
                tradeDetail,
                entry.getCreatedAt());
    }
}
