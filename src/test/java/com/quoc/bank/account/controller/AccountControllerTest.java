package com.quoc.bank.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quoc.bank.account.dto.AccountCreationRequest;
import com.quoc.bank.account.dto.AccountDepositRequest;
import com.quoc.bank.account.dto.AccountResponse;
import com.quoc.bank.account.dto.BalanceResponse;
import com.quoc.bank.account.entity.AccountType;
import com.quoc.bank.account.service.AccountService;
import com.quoc.bank.exception.AccountNotFoundException;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @BeforeEach
    void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("API: Tạo tài khoản thành công - HTTP 201")
    void createAccount_api_success() throws Exception {
        UUID customerId = UUID.randomUUID();
        AccountCreationRequest request = AccountCreationRequest.builder()
                .customerId(customerId)
                .accountNumber("001100001")
                .accountType(AccountType.PAYMENT)
                .initialBalance(new BigDecimal("10000000"))
                .dailyLimit(new BigDecimal("50000000"))
                .build();

        AccountResponse response = AccountResponse.builder()
                .id(UUID.randomUUID())
                .accountNumber("001100001")
                .customerId(customerId)
                .accountType(AccountType.PAYMENT)
                .balance(new BigDecimal("10000000"))
                .dailyLimit(new BigDecimal("50000000"))
                .build();

        when(accountService.createAccount(any(AccountCreationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.accountNumber").value("001100001"));
    }

    @Test
    @DisplayName("API: Nạp tiền thành công - HTTP 200")
    void deposit_api_success() throws Exception {
        AccountDepositRequest request = AccountDepositRequest.builder()
                .amount(new BigDecimal("2000000"))
                .build();

        AccountResponse response = AccountResponse.builder()
                .accountNumber("001100001")
                .balance(new BigDecimal("12000000"))
                .build();

        when(accountService.deposit(eq("001100001"), any(BigDecimal.class))).thenReturn(response);

        mockMvc.perform(post("/api/accounts/001100001/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.balance").value(12000000));
    }

    @Test
    @DisplayName("API: Kiểm tra số dư - HTTP 200")
    void getBalance_api_success() throws Exception {
        BalanceResponse response = BalanceResponse.builder()
                .accountNumber("001100001")
                .balance(new BigDecimal("10000000"))
                .build();

        when(accountService.getBalance("001100001")).thenReturn(response);

        mockMvc.perform(get("/api/accounts/001100001/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.balance").value(10000000));
    }

    @Test
    @DisplayName("API: Tìm tài khoản không tồn tại - HTTP 404 và code 1021")
    void getBalance_accountNotFound_returns404() throws Exception {
        when(accountService.getBalance("999999999"))
                .thenThrow(new AccountNotFoundException("Không tìm thấy tài khoản: 999999999"));

        mockMvc.perform(get("/api/accounts/999999999/balance"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1021));
    }
}
