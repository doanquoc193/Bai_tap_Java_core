package com.quoc.bank.repository;

import com.quoc.bank.model.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountRepository {

    private final List<Account> accounts =
            new ArrayList<>();

    // Thêm tài khoản
    public void save(Account account) {
        accounts.add(account);
    }

    // Tìm tài khoản theo số tài khoản
    public Optional<Account> findByAccountNumber(
            String accountNumber) {

        return accounts.stream()
                .filter(account ->
                        account.getAccountNumber()
                                .equals(accountNumber))
                .findFirst();
    }

    // Kiểm tra tài khoản có tồn tại hay không
    public boolean existsByAccountNumber(
            String accountNumber) {

        return accounts.stream()
                .anyMatch(account ->
                        account.getAccountNumber()
                                .equals(accountNumber));
    }

    // Lấy tất cả tài khoản
    public List<Account> findAll() {
        return accounts;
    }
}