package com.quoc.bank.transaction.dto;

import com.quoc.bank.transaction.entity.TransactionStatus;
import com.quoc.bank.transaction.entity.TransactionType;
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
public class TransactionResponse {

    UUID id;
    String referenceNumber;
    String fromAccountNumber;
    String toAccountNumber;
    TransactionType transactionType;
    BigDecimal amount;
    TransactionStatus status;
    String failureReason;
    LocalDateTime createdAt;
}
