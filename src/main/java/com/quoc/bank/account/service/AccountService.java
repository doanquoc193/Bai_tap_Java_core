package com.quoc.bank.account.service;

import com.quoc.bank.account.dto.AccountCreationRequest;
import com.quoc.bank.account.dto.AccountResponse;
import com.quoc.bank.account.dto.BalanceResponse;
import com.quoc.bank.account.entity.Account;
import com.quoc.bank.account.entity.PaymentAccount;
import com.quoc.bank.account.entity.SavingAccount;
import com.quoc.bank.account.repository.AccountRepository;
import com.quoc.bank.common.exception.AppException;
import com.quoc.bank.common.exception.ErrorCode;
import com.quoc.bank.customer.entity.Customer;
import com.quoc.bank.customer.repository.CustomerRepository;
import com.quoc.bank.exception.AccountNotFoundException;
import com.quoc.bank.exception.CustomerNotFoundException;
import com.quoc.bank.exception.DuplicateAccountException;
import com.quoc.bank.exception.InvalidAmountException;
import com.quoc.bank.transaction.entity.Transaction;
import com.quoc.bank.transaction.entity.TransactionStatus;
import com.quoc.bank.transaction.entity.TransactionType;
import com.quoc.bank.transaction.repository.TransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountService {

    AccountRepository accountRepository;
    CustomerRepository customerRepository;
    TransactionRepository transactionRepository;

    @Transactional
    public AccountResponse createAccount(AccountCreationRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException("Không tìm thấy khách hàng với ID: " + request.getCustomerId()));

        if (accountRepository.existsByAccountNumber(request.getAccountNumber())) {
            throw new DuplicateAccountException("Số tài khoản đã tồn tại: " + request.getAccountNumber());
        }

        if (request.getInitialBalance() == null || request.getInitialBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Số dư ban đầu không được âm");
        }

        if (request.getDailyLimit() == null || request.getDailyLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Hạn mức giao dịch trong ngày phải lớn hơn 0");
        }

        Account account;
        switch (request.getAccountType()) {
            case PAYMENT -> account = new PaymentAccount();
            case SAVING -> account = new SavingAccount();
            default -> throw new AppException(ErrorCode.INVALID_ACCOUNT_TYPE);
        }

        account.setAccountNumber(request.getAccountNumber().trim());
        account.setOwner(customer);
        account.setBalance(request.getInitialBalance());
        account.setDailyLimit(request.getDailyLimit());

        Account savedAccount = accountRepository.save(account);
        log.info("Created account successfully: number={}, type={}, customerId={}",
                savedAccount.getAccountNumber(), savedAccount.getType(), customer.getId());

        return toAccountResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Không tìm thấy tài khoản với ID: " + id));
        return toAccountResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Không tìm thấy tài khoản: " + accountNumber));
        return toAccountResponse(account);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Không tìm thấy tài khoản: " + accountNumber));

        return BalanceResponse.builder()
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomerId(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Không tìm thấy khách hàng với ID: " + customerId);
        }
        return accountRepository.findByOwnerId(customerId).stream()
                .map(this::toAccountResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> getAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(this::toAccountResponse);
    }

    @Transactional
    public AccountResponse deposit(String accountNumber, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Số tiền nạp phải lớn hơn 0");
        }

        Account account = accountRepository.findByAccountNumberWithLock(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Không tìm thấy tài khoản: " + accountNumber));

        account.deposit(amount);
        Account savedAccount = accountRepository.save(account);

        Transaction depositTx = Transaction.builder()
                .referenceNumber("TX-DEP-" + UUID.randomUUID())
                .fromAccount(null)
                .toAccount(savedAccount)
                .transactionType(TransactionType.DEPOSIT)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .build();

        transactionRepository.save(depositTx);
        log.info("Deposit successful: account={}, amount={}, newBalance={}",
                accountNumber, amount, savedAccount.getBalance());

        return toAccountResponse(savedAccount);
    }

    public AccountResponse toAccountResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getOwner().getId())
                .customerName(account.getOwner().getFullName())
                .customerCode(account.getOwner().getCustomerCode())
                .accountType(account.getType())
                .balance(account.getBalance())
                .dailyLimit(account.getDailyLimit())
                .description(account.getDescription())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
