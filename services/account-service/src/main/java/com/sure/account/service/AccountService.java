package com.sure.account.service;

import com.sure.account.dto.*;
import com.sure.account.entity.Account;
import com.sure.account.entity.Balance;
import com.sure.account.entity.Holding;
import com.sure.account.repository.AccountRepository;
import com.sure.account.repository.BalanceRepository;
import com.sure.account.repository.HoldingRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final HoldingRepository holdingRepository;

    public AccountService(
            AccountRepository accountRepository,
            BalanceRepository balanceRepository,
            HoldingRepository holdingRepository) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.holdingRepository = holdingRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getAccounts(UUID familyId) {
        return accountRepository.findByFamilyIdAndActiveTrue(familyId).stream()
                .map(this::toAccountDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountDto getAccount(UUID accountId, UUID familyId) {
        Account account = accountRepository
                .findById(accountId)
                .filter(a -> a.getFamilyId().equals(familyId))
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return toAccountDto(account);
    }

    @Transactional
    public AccountDto createAccount(UUID familyId, CreateAccountRequest request) {
        Account account = Account.builder()
                .familyId(familyId)
                .name(request.name())
                .accountType(request.accountType())
                .subtype(request.subtype())
                .currency(request.currency() != null ? request.currency() : "USD")
                .balance(request.balance() != null ? request.balance() : java.math.BigDecimal.ZERO)
                .institutionName(request.institutionName())
                .institutionUrl(request.institutionUrl())
                .build();
        account = accountRepository.save(account);
        return toAccountDto(account);
    }

    @Transactional
    public AccountDto updateAccount(UUID accountId, UUID familyId, UpdateAccountRequest request) {
        Account account = accountRepository
                .findById(accountId)
                .filter(a -> a.getFamilyId().equals(familyId))
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (request.name() != null) account.setName(request.name());
        if (request.subtype() != null) account.setSubtype(request.subtype());
        if (request.balance() != null) account.setBalance(request.balance());
        if (request.institutionName() != null) account.setInstitutionName(request.institutionName());
        if (request.excludedFromTotals() != null) account.setExcludedFromTotals(request.excludedFromTotals());

        account = accountRepository.save(account);
        return toAccountDto(account);
    }

    @Transactional
    public void deleteAccount(UUID accountId, UUID familyId) {
        Account account = accountRepository
                .findById(accountId)
                .filter(a -> a.getFamilyId().equals(familyId))
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.setActive(false);
        account.setScheduledForDeletion(true);
        accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public List<BalanceDto> getBalanceHistory(UUID accountId, LocalDate start, LocalDate end) {
        return balanceRepository.findByAccountIdAndDateBetweenOrderByDateAsc(accountId, start, end).stream()
                .map(this::toBalanceDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HoldingDto> getHoldings(UUID accountId) {
        return holdingRepository.findByAccountId(accountId).stream()
                .map(this::toHoldingDto)
                .collect(Collectors.toList());
    }

    private AccountDto toAccountDto(Account account) {
        return new AccountDto(
                account.getId(),
                account.getFamilyId(),
                account.getName(),
                account.getAccountType(),
                account.getSubtype(),
                account.getCurrency(),
                account.getBalance(),
                account.getStatus(),
                account.isActive(),
                account.getInstitutionName(),
                account.getLogoUrl(),
                account.isExcludedFromTotals(),
                account.getCreatedAt());
    }

    private BalanceDto toBalanceDto(Balance balance) {
        return new BalanceDto(
                balance.getId(),
                balance.getAccount().getId(),
                balance.getDate(),
                balance.getBalance(),
                balance.getCurrency());
    }

    private HoldingDto toHoldingDto(Holding holding) {
        return new HoldingDto(
                holding.getId(),
                holding.getAccount().getId(),
                holding.getSecurity() != null ? holding.getSecurity().getId() : null,
                holding.getSecurity() != null ? holding.getSecurity().getTicker() : null,
                holding.getSecurity() != null ? holding.getSecurity().getName() : holding.getName(),
                holding.getDate(),
                holding.getQty(),
                holding.getPrice(),
                holding.getAmount(),
                holding.getCurrency(),
                holding.getCostBasis(),
                holding.getCostBasisSource());
    }
}
