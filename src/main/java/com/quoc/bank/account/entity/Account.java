package com.quoc.bank.account.entity;

import com.quoc.bank.customer.entity.Customer;
import com.quoc.bank.exception.InsufficientBalanceException;
import com.quoc.bank.exception.InvalidAmountException;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "account_number", nullable = false, unique = true, length = 50)
    String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    Customer owner;

    @Column(name = "account_type", insertable = false, updatable = false, length = 30)
    String accountType;

    @Column(nullable = false, precision = 19, scale = 2)
    BigDecimal balance;

    @Column(name = "daily_limit", nullable = false, precision = 19, scale = 2)
    BigDecimal dailyLimit;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    public abstract String getDescription();

    public abstract AccountType getType();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Số tiền nạp phải lớn hơn 0");
        }
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Số tiền rút phải lớn hơn 0");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Số dư tài khoản " + this.accountNumber + " không đủ");
        }
        this.balance = this.balance.subtract(amount);
    }
}
