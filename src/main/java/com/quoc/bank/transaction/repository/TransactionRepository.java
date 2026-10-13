package com.quoc.bank.transaction.repository;

import com.quoc.bank.account.entity.Account;
import com.quoc.bank.transaction.entity.Transaction;
import com.quoc.bank.transaction.entity.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    boolean existsByReferenceNumber(String referenceNumber);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.fromAccount = :fromAccount AND t.status = 'SUCCESS' " +
            "AND t.createdAt >= :startOfDay AND t.createdAt < :endOfDay")
    BigDecimal calculateDailyTransferred(
            @Param("fromAccount") Account fromAccount,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.status = 'SUCCESS'")
    BigDecimal getTotalSuccessAmount();

    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.accountNumber = :accountNumber OR t.toAccount.accountNumber = :accountNumber")
    Page<Transaction> findByAccountNumber(@Param("accountNumber") String accountNumber, Pageable pageable);

    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);

    Page<Transaction> findByStatusAndCreatedAtBetween(
            TransactionStatus status,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    Page<Transaction> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    Optional<Transaction> findFirstByStatusOrderByAmountDesc(TransactionStatus status);

    List<Transaction> findByStatus(TransactionStatus status);

    long countByFromAccountIdOrToAccountId(UUID fromAccountId, UUID toAccountId);
}
