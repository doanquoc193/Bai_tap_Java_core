package com.quoc.bank.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferRequest {

    @NotBlank(message = "Tài khoản nguồn không được để trống")
    String fromAccountNumber;

    @NotBlank(message = "Tài khoản nhận không được để trống")
    String toAccountNumber;

    @NotNull(message = "Số tiền chuyển không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền chuyển phải lớn hơn 0")
    BigDecimal amount;
}
