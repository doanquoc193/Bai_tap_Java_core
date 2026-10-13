package com.quoc.bank.account.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("PAYMENT")
@NoArgsConstructor
public class PaymentAccount extends Account {

    @Override
    public String getDescription() {
        return "Tài khoản thanh toán";
    }

    @Override
    public AccountType getType() {
        return AccountType.PAYMENT;
    }
}
