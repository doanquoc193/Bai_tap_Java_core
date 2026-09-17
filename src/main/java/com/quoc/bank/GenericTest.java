package com.quoc.bank;

import com.quoc.bank.model.Account;
import com.quoc.bank.model.Customer;
import com.quoc.bank.model.PaymentAccount;
import com.quoc.bank.model.Transaction;
import com.quoc.bank.model.TransactionStatus;
import com.quoc.bank.response.ApiResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class GenericTest {

    public static void main(String[] args) {

        // ========================================
        // 1. ApiResponse<Customer>
        // ========================================

        Customer customer =
                new Customer(
                        "QUOC01",
                        "Doan Quoc"
                );

        ApiResponse<Customer> customerResponse =
                new ApiResponse<>(
                        1000,
                        "Lay customer thanh cong",
                        customer
                );

        System.out.println(
                "CUSTOMER RESPONSE:"
        );

        System.out.println(customerResponse);


        // ========================================
        // 2. ApiResponse<Account>
        // ========================================

        Account account =
                new PaymentAccount(
                        "ABC001",
                        customer,
                        new BigDecimal("5000000"),
                        new BigDecimal("3000000")
                );

        ApiResponse<Account> accountResponse =
                new ApiResponse<>(
                        1000,
                        "Lay account thanh cong",
                        account
                );

        System.out.println();
        System.out.println(
                "ACCOUNT RESPONSE:"
        );

        System.out.println(accountResponse);


        // ========================================
        // 3. ApiResponse<Transaction>
        // ========================================

        Transaction transaction =
                new Transaction(
                        "TX001",
                        account,
                        account,
                        new BigDecimal("1000000"),
                        TransactionStatus.SUCCESS,
                        LocalDateTime.now(),
                        null
                );

        ApiResponse<Transaction> transactionResponse =
                new ApiResponse<>(
                        1000,
                        "Lay transaction thanh cong",
                        transaction
                );

        System.out.println();
        System.out.println(
                "TRANSACTION RESPONSE:"
        );

        System.out.println(transactionResponse);


        // ========================================
        // 4. ApiResponse<List<Transaction>>
        // ========================================

        List<Transaction> transactions =
                List.of(transaction);

        ApiResponse<List<Transaction>>
                transactionListResponse =
                new ApiResponse<>(
                        1000,
                        "Lay danh sach transaction thanh cong",
                        transactions
                );

        System.out.println();
        System.out.println(
                "TRANSACTION LIST RESPONSE:"
        );

        System.out.println(
                transactionListResponse
        );
    }
}