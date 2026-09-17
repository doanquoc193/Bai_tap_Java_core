package com.quoc.bank.service;

import com.quoc.bank.exception.InsufficientBalanceException;
import com.quoc.bank.exception.InvalidAmountException;
import com.quoc.bank.exception.InvalidTransactionException;
import com.quoc.bank.exception.TransactionLimitExceededException;
import com.quoc.bank.model.Account;
import com.quoc.bank.model.Transaction;
import com.quoc.bank.model.TransactionStatus;
import com.quoc.bank.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionService {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    public TransactionService(
            AccountService accountService,
            TransactionRepository transactionRepository) {

        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
    }

    public void transfer(
            String fromAccountNumber,
            String toAccountNumber,
            BigDecimal amount) {

        // 1. Kiểm tra số tài khoản nguồn và đích
        if (fromAccountNumber == null ||
                fromAccountNumber.isBlank()) {

            throw new InvalidTransactionException(
                    "Tài khoản nguồn không được để trống"
            );
        }

        if (toAccountNumber == null ||
                toAccountNumber.isBlank()) {

            throw new InvalidTransactionException(
                    "Tài khoản đích không được để trống"
            );
        }

        // 2. Kiểm tra số tiền
        validateAmount(amount);

        // 3. Không cho chuyển cho chính mình
        if (fromAccountNumber.equals(toAccountNumber)) {

            throw new InvalidTransactionException(
                    "Không thể chuyển tiền cho chính tài khoản đó"
            );
        }

        // 4. Tìm tài khoản nguồn
        Account fromAccount =
                accountService.getAccount(fromAccountNumber);

        // 5. Tìm tài khoản đích
        Account toAccount =
                accountService.getAccount(toAccountNumber);

        // 6. Kiểm tra số dư
        if (fromAccount.getBalance().compareTo(amount) < 0) {

            throw new InsufficientBalanceException(
                    "Số dư tài khoản "
                            + fromAccountNumber
                            + " không đủ"
            );
        }

        // 7. Kiểm tra hạn mức trong ngày
        BigDecimal dailyTransferred =
                calculateDailyTransferred(fromAccountNumber);

        if (dailyTransferred.add(amount)
                .compareTo(fromAccount.getDailyLimit()) > 0) {

            throw new TransactionLimitExceededException(
                    "Giao dịch vượt quá hạn mức trong ngày"
            );
        }

        // 8. Trừ tiền tài khoản nguồn
        fromAccount.withdraw(amount);

        // 9. Cộng tiền tài khoản đích
        toAccount.deposit(amount);

        // 10. Tạo mã giao dịch
        String refNo = generateRefNo();

        // 11. Tạo transaction thành công
        Transaction transaction = new Transaction(
                refNo,
                fromAccount,
                toAccount,
                amount,
                TransactionStatus.SUCCESS,
                LocalDateTime.now(),
                null
        );

        // 12. Lưu transaction
        transactionRepository.save(transaction);
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null) {
            throw new InvalidAmountException(
                    "Số tiền giao dịch không được null"
            );
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException(
                    "Số tiền giao dịch phải lớn hơn 0"
            );
        }
    }

    private BigDecimal calculateDailyTransferred(
            String accountNumber) {

        BigDecimal total = BigDecimal.ZERO;

        LocalDate today = LocalDate.now();

        for (Transaction transaction :
                transactionRepository.findAll()) {

            // Chỉ tính transaction thành công
            if (transaction.getStatus() != TransactionStatus.SUCCESS) {
                continue;
            }

            // Chỉ tính transaction của tài khoản nguồn
            if (!transaction.getFromAccount()
                    .getAccountNumber()
                    .equals(accountNumber)) {
                continue;
            }

            // Chỉ tính transaction trong ngày hôm nay
            if (!transaction.getCreatedAt()
                    .toLocalDate()
                    .equals(today)) {
                continue;
            }

            total = total.add(transaction.getAmount());
        }

        return total;
    }

    private String generateRefNo() {
        return "TX-" + UUID.randomUUID();
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getSuccessfulTransactions() {

        return transactionRepository.findAll()
                .stream()
                .filter(transaction ->
                        transaction.getStatus()
                                == TransactionStatus.SUCCESS)
                .toList();
    }

    public BigDecimal getTotalSuccessfulAmount() {

        return transactionRepository.findAll()
                .stream()
                .filter(transaction ->
                        transaction.getStatus()
                                == TransactionStatus.SUCCESS)
                .map(Transaction::getAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    public Transaction getLargestSuccessfulTransaction() {

        return transactionRepository.findAll()
                .stream()
                .filter(transaction ->
                        transaction.getStatus()
                                == TransactionStatus.SUCCESS)
                .max((t1, t2) ->
                        t1.getAmount().compareTo(t2.getAmount()))
                .orElseThrow(() ->
                        new InvalidTransactionException(
                                "Không có giao dịch SUCCESS"
                        )
                );
    }

    public List<Transaction> getTransactionsSortedByAmountDesc() {

        return transactionRepository.findAll()
                .stream()
                .sorted((t1, t2) ->
                        t2.getAmount().compareTo(t1.getAmount()))
                .toList();
    }

    public Map<String, List<Transaction>> groupTransactionsByFromAccount() {

        return transactionRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        transaction ->
                                transaction.getFromAccount()
                                        .getAccountNumber()
                ));
    }


    public List<Transaction> getFailedTransactions() {

        return transactionRepository.findAll()
                .stream()
                .filter(transaction ->
                        transaction.getStatus()
                                == TransactionStatus.FAILED)
                .toList();
    }

    public Transaction getTransactionByRefNo(String refNo) {

        return transactionRepository
                .findByRefNo(refNo)
                .orElseThrow(() ->
                        new InvalidTransactionException(
                                "Không tìm thấy giao dịch: " + refNo
                        )
                );
    }
}