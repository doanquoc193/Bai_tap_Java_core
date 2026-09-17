package com.quoc.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private final String refNo;

    private final Account fromAccount;

    private final Account toAccount;

    private final BigDecimal amount;

    private final TransactionStatus status;

    private final LocalDateTime createdAt;

    private final String failureReason;

    public Transaction(
            String refNo,
            Account fromAccount,
            Account toAccount,
            BigDecimal amount,
            TransactionStatus status,
            LocalDateTime createdAt,
            String failureReason
    ) {
        this.refNo = refNo;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.failureReason = failureReason;
    }

    public String getRefNo() {
        return refNo;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFailureReason() {
        return failureReason;
    }
}