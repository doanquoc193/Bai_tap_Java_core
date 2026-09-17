package com.quoc.bank.model;

import java.math.BigDecimal;

public class SavingAccount extends Account {

    public SavingAccount(
            String accountNumber,
            Customer owner,
            BigDecimal balance,
            BigDecimal dailyLimit
    ) {
        super(
                accountNumber,
                owner,
                AccountType.SAVING,
                balance,
                dailyLimit
        );
    }

    @Override
    public String getDescription() {
        return "Tài khoản tiết kiệm";
    }
}