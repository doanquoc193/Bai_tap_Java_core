package com.quoc.bank.transaction.service;

import com.quoc.bank.account.entity.Account;
import com.quoc.bank.account.entity.PaymentAccount;
import com.quoc.bank.account.entity.SavingAccount;
import com.quoc.bank.account.repository.AccountRepository;
import com.quoc.bank.common.exception.AppException;
import com.quoc.bank.customer.entity.Customer;
import com.quoc.bank.exception.AccountNotFoundException;
import com.quoc.bank.exception.InsufficientBalanceException;
import com.quoc.bank.exception.TransactionLimitExceededException;
import com.quoc.bank.transaction.dto.TransactionResponse;
import com.quoc.bank.transaction.dto.TransferRequest;
import com.quoc.bank.transaction.entity.Transaction;
import com.quoc.bank.transaction.entity.TransactionStatus;
import com.quoc.bank.transaction.entity.TransactionType;
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
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionFailureLogger failureLogger;

    @InjectMocks
    private TransactionService transactionService;

    private PaymentAccount sourceAccount;
    private SavingAccount targetAccount;

    @BeforeEach
    void setUp() {
        Customer c1 = Customer.builder()
                .id(UUID.randomUUID())
                .customerCode("C001")
                .fullName("Nguyen Van A")
                .build();

        Customer c2 = Customer.builder()
                .id(UUID.randomUUID())
                .customerCode("C002")
                .fullName("Tran Thi B")
                .build();

        sourceAccount = new PaymentAccount();
        sourceAccount.setId(UUID.randomUUID());
        sourceAccount.setAccountNumber("001");
        sourceAccount.setOwner(c1);
        sourceAccount.setBalance(new BigDecimal("100000000"));
        sourceAccount.setDailyLimit(new BigDecimal("50000000"));
        sourceAccount.setCreatedAt(LocalDateTime.now());
        sourceAccount.setUpdatedAt(LocalDateTime.now());

        targetAccount = new SavingAccount();
        targetAccount.setId(UUID.randomUUID());
        targetAccount.setAccountNumber("002");
        targetAccount.setOwner(c2);
        targetAccount.setBalance(new BigDecimal("20000000"));
        targetAccount.setDailyLimit(new BigDecimal("30000000"));
        targetAccount.setCreatedAt(LocalDateTime.now());
        targetAccount.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Chuyển tiền thành công giữa 2 tài khoản")
    void transfer_success() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("001")
                .toAccountNumber("002")
                .amount(new BigDecimal("3000000"))
                .build();

        when(accountRepository.findByAccountNumberWithLock("001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberWithLock("002")).thenReturn(Optional.of(targetAccount));
        when(transactionRepository.calculateDailyTransferred(eq(sourceAccount), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        Transaction mockTx = Transaction.builder()
                .id(UUID.randomUUID())
                .referenceNumber("TX-12345")
                .fromAccount(sourceAccount)
                .toAccount(targetAccount)
                .transactionType(TransactionType.TRANSFER)
                .amount(new BigDecimal("3000000"))
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTx);

        TransactionResponse response = transactionService.transfer(request);

        assertNotNull(response);
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(new BigDecimal("97000000"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("23000000"), targetAccount.getBalance());
        verify(accountRepository, times(1)).save(sourceAccount);
        verify(accountRepository, times(1)).save(targetAccount);
    }

    @Test
    @DisplayName("Chuyển tiền cùng tài khoản - ném AppException")
    void transfer_sameAccount_throwsException() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("001")
                .toAccountNumber("001")
                .amount(new BigDecimal("1000000"))
                .build();

        assertThrows(AppException.class, () -> transactionService.transfer(request));
        verify(accountRepository, never()).findByAccountNumberWithLock(any());
    }

    @Test
    @DisplayName("Chuyển tiền khi số dư không đủ - ghi nhận FAILED và ném InsufficientBalanceException")
    void transfer_insufficientBalance_recordsFailedTransaction_andThrows() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("001")
                .toAccountNumber("002")
                .amount(new BigDecimal("200000000"))
                .build();

        when(accountRepository.findByAccountNumberWithLock("001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberWithLock("002")).thenReturn(Optional.of(targetAccount));

        assertThrows(InsufficientBalanceException.class, () -> transactionService.transfer(request));

        // Balance must not change
        assertEquals(new BigDecimal("100000000"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("20000000"), targetAccount.getBalance());

        // Verifies FAILED transaction was logged independently
        verify(failureLogger, times(1)).logFailedTransaction(
                eq(sourceAccount), eq(targetAccount), eq(new BigDecimal("200000000")), anyString()
        );
    }

    @Test
    @DisplayName("Chuyển tiền vượt hạn mức trong ngày - ghi nhận FAILED và ném TransactionLimitExceededException")
    void transfer_dailyLimitExceeded_recordsFailedTransaction_andThrows() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("001")
                .toAccountNumber("002")
                .amount(new BigDecimal("15000000"))
                .build();

        when(accountRepository.findByAccountNumberWithLock("001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberWithLock("002")).thenReturn(Optional.of(targetAccount));
        // Đã chuyển 40 triệu trong ngày, hạn mức là 50 triệu, chuyển thêm 15 triệu -> tổng 55 triệu > 50 triệu
        when(transactionRepository.calculateDailyTransferred(eq(sourceAccount), any(), any()))
                .thenReturn(new BigDecimal("40000000"));

        assertThrows(TransactionLimitExceededException.class, () -> transactionService.transfer(request));

        assertEquals(new BigDecimal("100000000"), sourceAccount.getBalance());
        verify(failureLogger, times(1)).logFailedTransaction(
                eq(sourceAccount), eq(targetAccount), eq(new BigDecimal("15000000")), anyString()
        );
    }

    @Test
    @DisplayName("Chuyển tiền khi tài khoản đích không tồn tại - ném AccountNotFoundException")
    void transfer_accountNotFound_throwsException() {
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("001")
                .toAccountNumber("999")
                .amount(new BigDecimal("1000000"))
                .build();

        when(accountRepository.findByAccountNumberWithLock("001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberWithLock("999")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> transactionService.transfer(request));
    }
}
