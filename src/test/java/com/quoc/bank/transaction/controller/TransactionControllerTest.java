package com.quoc.bank.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quoc.bank.exception.InsufficientBalanceException;
import com.quoc.bank.transaction.dto.TransactionResponse;
import com.quoc.bank.transaction.dto.TransferRequest;
import com.quoc.bank.transaction.entity.TransactionStatus;
import com.quoc.bank.transaction.entity.TransactionType;
import com.quoc.bank.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @BeforeEach
    void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("API: Chuyển tiền thành công - HTTP 201 và code 1000")
    void transfer_api_success() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("001")
                .toAccountNumber("002")
                .amount(new BigDecimal("3000000"))
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id(UUID.randomUUID())
                .referenceNumber("TX-12345")
                .fromAccountNumber("001")
                .toAccountNumber("002")
                .transactionType(TransactionType.TRANSFER)
                .amount(new BigDecimal("3000000"))
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        when(transactionService.transfer(any(TransferRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.referenceNumber").value("TX-12345"))
                .andExpect(jsonPath("$.result.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("API: Chuyển tiền không đủ số dư - HTTP 400 và code 1031")
    void transfer_api_insufficientBalance_returns400() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("001")
                .toAccountNumber("002")
                .amount(new BigDecimal("999000000"))
                .build();

        when(transactionService.transfer(any(TransferRequest.class)))
                .thenThrow(new InsufficientBalanceException("Số dư tài khoản 001 không đủ"));

        mockMvc.perform(post("/api/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1031))
                .andExpect(jsonPath("$.message").value("Số dư tài khoản 001 không đủ"));
    }

    @Test
    @DisplayName("API: Lấy thông tin giao dịch theo refNo - HTTP 200")
    void getTransactionByRefNo_api_success() throws Exception {
        TransactionResponse response = TransactionResponse.builder()
                .id(UUID.randomUUID())
                .referenceNumber("TX-999")
                .fromAccountNumber("001")
                .toAccountNumber("002")
                .amount(new BigDecimal("500000"))
                .status(TransactionStatus.SUCCESS)
                .build();

        when(transactionService.getTransactionByRefNo("TX-999")).thenReturn(response);

        mockMvc.perform(get("/api/transactions/TX-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.referenceNumber").value("TX-999"));
    }
}
