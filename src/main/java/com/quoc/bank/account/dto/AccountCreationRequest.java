package com.quoc.bank.account.dto;

import com.quoc.bank.account.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountCreationRequest {

    @NotNull(message = "Customer ID không được để trống")
    UUID customerId;

    @NotBlank(message = "Số tài khoản không được để trống")
    String accountNumber;

    @NotNull(message = "Loại tài khoản không được để trống (PAYMENT hoặc SAVING)")
    AccountType accountType;

    @NotNull(message = "Số dư ban đầu không được để trống")
    @DecimalMin(value = "0.0", message = "Số dư ban đầu không được âm")
    BigDecimal initialBalance;

    @NotNull(message = "Hạn mức trong ngày không được để trống")
    @DecimalMin(value = "0.01", message = "Hạn mức giao dịch trong ngày phải lớn hơn 0")
    BigDecimal dailyLimit;
}
