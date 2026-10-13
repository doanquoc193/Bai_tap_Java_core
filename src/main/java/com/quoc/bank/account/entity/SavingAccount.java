package com.quoc.bank.account.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("SAVING")
@NoArgsConstructor
public class SavingAccount extends Account {

    @Override
    public String getDescription() {
        return "Tài khoản tiết kiệm";
    }

    @Override
    public AccountType getType() {
        return AccountType.SAVING;
    }
}
