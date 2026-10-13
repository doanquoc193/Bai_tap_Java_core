package com.quoc.bank.transaction.service;

import com.quoc.bank.account.entity.Account;
import com.quoc.bank.account.repository.AccountRepository;
import com.quoc.bank.common.exception.AppException;
import com.quoc.bank.common.exception.ErrorCode;
import com.quoc.bank.exception.*;
import com.quoc.bank.transaction.dto.TransactionResponse;
import com.quoc.bank.transaction.dto.TransferRequest;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionService {

    AccountRepository accountRepository;
    TransactionRepository transactionRepository;
    TransactionFailureLogger failureLogger;

    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        String fromAccNum = request.getFromAccountNumber().trim();
        String toAccNum = request.getToAccountNumber().trim();
        BigDecimal amount = request.getAmount();

        if (fromAccNum.equalsIgnoreCase(toAccNum)) {
            throw new AppException(ErrorCode.SELF_TRANSFER_NOT_ALLOWED,
                    "Không thể chuyển tiền cho chính tài khoản đó");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Số tiền giao dịch phải lớn hơn 0");
        }

        // Lock in deterministic alphabetical order to avoid deadlock
        String firstLock = fromAccNum.compareTo(toAccNum) < 0 ? fromAccNum : toAccNum;
        String secondLock = fromAccNum.compareTo(toAccNum) < 0 ? toAccNum : fromAccNum;

        Account acc1 = accountRepository.findByAccountNumberWithLock(firstLock)
                .orElseThrow(() -> new AccountNotFoundException("Không tìm thấy tài khoản: " + firstLock));
        Account acc2 = accountRepository.findByAccountNumberWithLock(secondLock)
                .orElseThrow(() -> new AccountNotFoundException("Không tìm thấy tài khoản: " + secondLock));

        Account fromAccount = fromAccNum.equals(firstLock) ? acc1 : acc2;
        Account toAccount = toAccNum.equals(firstLock) ? acc1 : acc2;

        // Check balance
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            String reason = "Số dư tài khoản " + fromAccNum + " không đủ để thực hiện giao dịch";
            failureLogger.logFailedTransaction(fromAccount, toAccount, amount, reason);
            throw new InsufficientBalanceException(reason);
        }

        // Check daily limit
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        BigDecimal transferredToday = transactionRepository.calculateDailyTransferred(fromAccount, startOfDay, endOfDay);
        if (transferredToday.add(amount).compareTo(fromAccount.getDailyLimit()) > 0) {
            String reason = "Giao dịch vượt quá hạn mức trong ngày. Hạn mức: "
                    + fromAccount.getDailyLimit() + ", đã dùng: " + transferredToday + ", muốn chuyển thêm: " + amount;
            failureLogger.logFailedTransaction(fromAccount, toAccount, amount, reason);
            throw new TransactionLimitExceededException(reason);
        }

        // Mutate accounts
        fromAccount.withdraw(amount);
        toAccount.deposit(amount);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Record SUCCESS transaction
        Transaction successTx = Transaction.builder()
                .referenceNumber("TX-" + UUID.randomUUID())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .transactionType(TransactionType.TRANSFER)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .failureReason(null)
                .build();

        Transaction savedTx = transactionRepository.save(successTx);
        log.info("Transfer SUCCESS: ref={}, from={}, to={}, amount={}",
                savedTx.getReferenceNumber(), fromAccNum, toAccNum, amount);

        return toTransactionResponse(savedTx);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByRefNo(String refNo) {
        Transaction tx = transactionRepository.findByReferenceNumber(refNo)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND,
                        "Không tìm thấy giao dịch với mã tham chiếu: " + refNo));
        return toTransactionResponse(tx);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByAccount(String accountNumber, Pageable pageable) {
        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            throw new AccountNotFoundException("Không tìm thấy tài khoản: " + accountNumber);
        }
        return transactionRepository.findByAccountNumber(accountNumber, pageable)
                .map(this::toTransactionResponse);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(
            TransactionStatus status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    ) {
        if (status != null && fromDate != null && toDate != null) {
            return transactionRepository.findByStatusAndCreatedAtBetween(status, fromDate, toDate, pageable)
                    .map(this::toTransactionResponse);
        } else if (status != null) {
            return transactionRepository.findByStatus(status, pageable)
                    .map(this::toTransactionResponse);
        } else if (fromDate != null && toDate != null) {
            return transactionRepository.findByCreatedAtBetween(fromDate, toDate, pageable)
                    .map(this::toTransactionResponse);
        } else {
            return transactionRepository.findAll(pageable)
                    .map(this::toTransactionResponse);
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalSuccessAmount() {
        return transactionRepository.getTotalSuccessAmount();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getLargestSuccessTransaction() {
        Transaction tx = transactionRepository.findFirstByStatusOrderByAmountDesc(TransactionStatus.SUCCESS)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND,
                        "Chưa có giao dịch SUCCESS nào trong hệ thống"));
        return toTransactionResponse(tx);
    }

    @Transactional(readOnly = true)
    public Map<String, List<TransactionResponse>> groupSuccessTransactionsByFromAccount() {
        List<Transaction> successTransactions = transactionRepository.findByStatus(TransactionStatus.SUCCESS);
        return successTransactions.stream()
                .filter(tx -> tx.getFromAccount() != null)
                .map(this::toTransactionResponse)
                .collect(Collectors.groupingBy(TransactionResponse::getFromAccountNumber));
    }

    public TransactionResponse toTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .referenceNumber(transaction.getReferenceNumber())
                .fromAccountNumber(transaction.getFromAccount() != null ? transaction.getFromAccount().getAccountNumber() : null)
                .toAccountNumber(transaction.getToAccount() != null ? transaction.getToAccount().getAccountNumber() : null)
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .failureReason(transaction.getFailureReason())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
