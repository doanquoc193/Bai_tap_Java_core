package com.quoc.bank.account.service;

import com.quoc.bank.account.dto.AccountCreationRequest;
import com.quoc.bank.account.dto.AccountResponse;
import com.quoc.bank.account.dto.BalanceResponse;
import com.quoc.bank.account.entity.Account;
import com.quoc.bank.account.entity.AccountType;
import com.quoc.bank.account.entity.PaymentAccount;
import com.quoc.bank.account.entity.SavingAccount;
import com.quoc.bank.account.repository.AccountRepository;
import com.quoc.bank.customer.entity.Customer;
import com.quoc.bank.customer.repository.CustomerRepository;
import com.quoc.bank.exception.AccountNotFoundException;
import com.quoc.bank.exception.CustomerNotFoundException;
import com.quoc.bank.exception.DuplicateAccountException;
import com.quoc.bank.exception.InvalidAmountException;
import com.quoc.bank.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    private Customer customer;
    private PaymentAccount paymentAccount;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(UUID.randomUUID())
                .customerCode("CUST-001")
                .fullName("Nguyen Van A")
                .build();

        paymentAccount = new PaymentAccount();
        paymentAccount.setId(UUID.randomUUID());
        paymentAccount.setAccountNumber("001100001");
        paymentAccount.setOwner(customer);
        paymentAccount.setBalance(new BigDecimal("100000000"));
        paymentAccount.setDailyLimit(new BigDecimal("50000000"));
        paymentAccount.setCreatedAt(LocalDateTime.now());
        paymentAccount.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Tạo tài khoản thanh toán thành công")
    void createAccount_payment_success() {
        AccountCreationRequest request = AccountCreationRequest.builder()
                .customerId(customer.getId())
                .accountNumber("001100001")
                .accountType(AccountType.PAYMENT)
                .initialBalance(new BigDecimal("5000000"))
                .dailyLimit(new BigDecimal("20000000"))
                .build();

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(accountRepository.existsByAccountNumber("001100001")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(paymentAccount);

        AccountResponse response = accountService.createAccount(request);

        assertNotNull(response);
        assertEquals("001100001", response.getAccountNumber());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("Tạo tài khoản với số tài khoản bị trùng - ném DuplicateAccountException")
    void createAccount_duplicateNumber_throwsException() {
        AccountCreationRequest request = AccountCreationRequest.builder()
                .customerId(customer.getId())
                .accountNumber("001100001")
                .accountType(AccountType.PAYMENT)
                .initialBalance(BigDecimal.ZERO)
                .dailyLimit(new BigDecimal("10000000"))
                .build();

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(accountRepository.existsByAccountNumber("001100001")).thenReturn(true);

        assertThrows(DuplicateAccountException.class, () -> accountService.createAccount(request));
    }

    @Test
    @DisplayName("Tạo tài khoản với số dư ban đầu âm - ném InvalidAmountException")
    void createAccount_negativeBalance_throwsException() {
        AccountCreationRequest request = AccountCreationRequest.builder()
                .customerId(customer.getId())
                .accountNumber("001100002")
                .accountType(AccountType.SAVING)
                .initialBalance(new BigDecimal("-1000"))
                .dailyLimit(new BigDecimal("10000000"))
                .build();

        when(customerRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(accountRepository.existsByAccountNumber("001100002")).thenReturn(false);

        assertThrows(InvalidAmountException.class, () -> accountService.createAccount(request));
    }

    @Test
    @DisplayName("Nạp tiền hợp lệ - số dư tăng và ghi nhận giao dịch")
    void deposit_validAmount_success() {
        BigDecimal initialBalance = paymentAccount.getBalance();
        BigDecimal depositAmount = new BigDecimal("5000000");

        when(accountRepository.findByAccountNumberWithLock("001100001")).thenReturn(Optional.of(paymentAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(paymentAccount);

        AccountResponse response = accountService.deposit("001100001", depositAmount);

        assertNotNull(response);
        assertEquals(initialBalance.add(depositAmount), paymentAccount.getBalance());
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Nạp số tiền <= 0 - ném InvalidAmountException")
    void deposit_invalidAmount_throwsException() {
        assertThrows(InvalidAmountException.class,
                () -> accountService.deposit("001100001", BigDecimal.ZERO));
        assertThrows(InvalidAmountException.class,
                () -> accountService.deposit("001100001", new BigDecimal("-5000")));
    }

    @Test
    @DisplayName("Kiểm tra số dư tài khoản thành công")
    void getBalance_success() {
        when(accountRepository.findByAccountNumber("001100001")).thenReturn(Optional.of(paymentAccount));

        BalanceResponse balance = accountService.getBalance("001100001");

        assertNotNull(balance);
        assertEquals(new BigDecimal("100000000"), balance.getBalance());
    }
}
