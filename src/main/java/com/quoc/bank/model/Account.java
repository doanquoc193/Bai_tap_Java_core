package com.quoc.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.quoc.bank.exception.InvalidAmountException;
import com.quoc.bank.exception.InsufficientBalanceException;


public abstract class Account {

    private final String accountNumber;
    private final Customer owner;
    private final AccountType type;

    private BigDecimal balance;

    private final BigDecimal dailyLimit;

    private final LocalDateTime createdAt;

    protected Account(
            String accountNumber,
            Customer owner,
            AccountType type,
            BigDecimal balance,
            BigDecimal dailyLimit
    ) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.type = type;
        this.balance = balance;
        this.dailyLimit = dailyLimit;
        this.createdAt = LocalDateTime.now();
    }

    public abstract String getDescription();

    public String getAccountNumber() {
        return accountNumber;
    }

    public Customer getOwner() {
        return owner;
    }

    public AccountType getType() {
        return type;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void deposit(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidAmountException(
                    "Số tiền nạp phải lớn hơn 0"
            );
        }

        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidAmountException(
                    "Số tiền rút phải lớn hơn 0"
            );
        }

        if (balance.compareTo(amount) < 0) {

            throw new InsufficientBalanceException(
                    "Số dư không đủ"
            );
        }

        balance = balance.subtract(amount);
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", owner='" + owner.getFullName() + '\'' +
                ", type=" + type +
                ", balance=" + balance +
                ", dailyLimit=" + dailyLimit +
                ", createdAt=" + createdAt +
                ", description='" + getDescription() + '\'' +
                '}';
    }
}