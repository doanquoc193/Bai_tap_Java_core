package com.quoc.bank.account.dto;

import com.quoc.bank.account.entity.AccountType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountResponse {

    UUID id;
    String accountNumber;
    UUID customerId;
    String customerName;
    String customerCode;
    AccountType accountType;
    BigDecimal balance;
    BigDecimal dailyLimit;
    String description;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
