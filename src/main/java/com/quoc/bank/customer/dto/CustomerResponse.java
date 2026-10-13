package com.quoc.bank.customer.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerResponse {

    UUID id;
    String customerCode;
    String fullName;
    String email;
    String phoneNumber;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    int accountCount;
}
