package com.quoc.bank.model;

import java.util.ArrayList;
import java.util.List;

public class Customer {

    // ==============================
    // THUỘC TÍNH
    // ==============================

    private final String customerId;

    private String fullName;

    // Một Customer có thể sở hữu nhiều Account
    private final List<Account> accounts = new ArrayList<>();


    // ==============================
    // CONSTRUCTOR
    // ==============================

    public Customer(
            String customerId,
            String fullName) {

        this.customerId = customerId;
        this.fullName = fullName;
    }


    // ==============================
    // GETTER / SETTER
    // ==============================

    public String getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Account> getAccounts() {
        return accounts;
    }


    // ==============================
    // THÊM ACCOUNT
    // ==============================

    public void addAccount(Account account) {

        if (account == null) {
            throw new IllegalArgumentException(
                    "Account không được null"
            );
        }

        accounts.add(account);
    }


    // ==============================
    // TO STRING
    // ==============================

    @Override
    public String toString() {

        return "Customer{" +
                "customerId='" + customerId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", accounts=" + accounts.size() +
                '}';
    }
}

