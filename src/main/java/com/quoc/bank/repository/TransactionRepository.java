package com.quoc.bank.repository;

import com.quoc.bank.model.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionRepository {

    private final List<Transaction> transactions =
            new ArrayList<>();

    // Lưu giao dịch
    public void save(Transaction transaction) {
        transactions.add(transaction);
    }

    // Lấy tất cả giao dịch
    public List<Transaction> findAll() {
        return transactions;
    }

    // Tìm giao dịch theo mã giao dịch
    public Optional<Transaction> findByRefNo(
            String refNo) {

        return transactions.stream()
                .filter(transaction ->
                        transaction.getRefNo()
                                .equals(refNo))
                .findFirst();
    }
}