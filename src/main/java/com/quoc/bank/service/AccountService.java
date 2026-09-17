package com.quoc.bank.service;

import com.quoc.bank.exception.AccountNotFoundException;
import com.quoc.bank.exception.DuplicateAccountException;
import com.quoc.bank.exception.InvalidAmountException;
import com.quoc.bank.model.Account;
import com.quoc.bank.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.List;

public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // ==========================================
    // TẠO ACCOUNT
    // ==========================================

    public void createAccount(Account account) {

        // 1. Kiểm tra Account không được null
        if (account == null) {
            throw new IllegalArgumentException(
                    "Account không được null"
            );
        }

        if (account.getOwner() == null) {
            throw new IllegalArgumentException(
                    "Account phai co chu tai khoan"
            );
        }

        if (account.getAccountNumber() == null ||
                account.getAccountNumber().isBlank()) {

            throw new IllegalArgumentException(
                    "Số tài khoản không được để trống"
            );
        }

        // 2. Kiểm tra số tài khoản đã tồn tại
        if (accountRepository.existsByAccountNumber(
                account.getAccountNumber())) {

            throw new DuplicateAccountException(
                    "Số tài khoản đã tồn tại: "
                            + account.getAccountNumber()
            );
        }

        // 3. Kiểm tra số dư ban đầu
        if (account.getBalance() == null ||
                account.getBalance()
                        .compareTo(BigDecimal.ZERO) < 0) {

            throw new InvalidAmountException(
                    "Số dư ban đầu không được âm"
            );
        }

        // 4. Kiểm tra hạn mức giao dịch trong ngày
        if (account.getDailyLimit() == null ||
                account.getDailyLimit()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidAmountException(
                    "Hạn mức giao dịch trong ngày phải lớn hơn 0"
            );
        }

        // 5. Thêm Account vào danh sách Account của Customer
        account.getOwner().addAccount(account);

        // 6. Lưu Account vào Repository
        accountRepository.save(account);
    }


    // ==========================================
    // TÌM ACCOUNT
    // ==========================================

    public Account getAccount(String accountNumber) {

        return accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Không tìm thấy tài khoản: "
                                        + accountNumber
                        )
                );
    }


    // ==========================================
    // LẤY TẤT CẢ ACCOUNT
    // ==========================================

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }


    // ==========================================
    // NẠP TIỀN
    // ==========================================

    public void deposit(
            String accountNumber,
            BigDecimal amount) {

        Account account = getAccount(accountNumber);

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidAmountException(
                    "Số tiền nạp phải lớn hơn 0"
            );
        }

        account.deposit(amount);
    }
}

