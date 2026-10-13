package com.quoc.bank.transaction.service;

import com.quoc.bank.account.entity.Account;
import com.quoc.bank.transaction.entity.Transaction;
import com.quoc.bank.transaction.entity.TransactionStatus;
import com.quoc.bank.transaction.entity.TransactionType;
import com.quoc.bank.transaction.repository.TransactionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionFailureLogger {

    TransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction logFailedTransaction(
            Account fromAccount,
            Account toAccount,
            BigDecimal amount,
            String failureReason
    ) {
        Transaction failedTx = Transaction.builder()
                .referenceNumber("TX-FAIL-" + UUID.randomUUID())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .transactionType(TransactionType.TRANSFER)
                .amount(amount != null ? amount : BigDecimal.ZERO)
                .status(TransactionStatus.FAILED)
                .failureReason(failureReason)
                .build();

        Transaction saved = transactionRepository.save(failedTx);
        log.warn("Recorded FAILED transaction: ref={}, reason={}", saved.getReferenceNumber(), failureReason);
        return saved;
    }
}
