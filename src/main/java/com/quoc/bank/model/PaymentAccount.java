package com.quoc.bank.model;

import java.math.BigDecimal;

public class PaymentAccount extends Account {

    public PaymentAccount(
            String accountNumber,
            Customer owner,
            BigDecimal balance,
            BigDecimal dailyLimit
    ) {
        super(
                accountNumber,
                owner,
                AccountType.PAYMENT,
                balance,
                dailyLimit
        );
    }

    @Override
    public String getDescription() {
        return "Tài khoản thanh toán";
    }
}