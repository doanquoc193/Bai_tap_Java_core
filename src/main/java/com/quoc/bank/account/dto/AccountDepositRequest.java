package com.quoc.bank.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountDepositRequest {

    @NotNull(message = "Số tiền nạp không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền nạp phải lớn hơn 0")
    BigDecimal amount;
}
