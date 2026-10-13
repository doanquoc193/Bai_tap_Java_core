package com.quoc.bank.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quoc.bank.customer.dto.CustomerCreationRequest;
import com.quoc.bank.customer.dto.CustomerResponse;
import com.quoc.bank.customer.service.CustomerService;
import com.quoc.bank.exception.DuplicateCustomerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @BeforeEach
    void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("API: Tạo khách hàng thành công - HTTP 201 và code 1000")
    void createCustomer_api_success() throws Exception {
        CustomerCreationRequest request = CustomerCreationRequest.builder()
                .customerCode("C001")
                .fullName("Nguyen Van A")
                .email("vana@example.com")
                .phoneNumber("0901234567")
                .build();

        CustomerResponse response = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .customerCode("C001")
                .fullName("Nguyen Van A")
                .email("vana@example.com")
                .phoneNumber("0901234567")
                .build();

        when(customerService.createCustomer(any(CustomerCreationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.customerCode").value("C001"))
                .andExpect(jsonPath("$.result.fullName").value("Nguyen Van A"));
    }

    @Test
    @DisplayName("API: Validate dữ liệu đầu vào thất bại - HTTP 400")
    void createCustomer_api_invalidInput_returns400() throws Exception {
        CustomerCreationRequest request = CustomerCreationRequest.builder()
                .customerCode("") // Invalid: blank
                .fullName("")     // Invalid: blank
                .email("invalid-email-format") // Invalid email
                .build();

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1040));
    }

    @Test
    @DisplayName("API: Trùng mã khách hàng - HTTP 400 và code 1012")
    void createCustomer_api_duplicateCode_returns400() throws Exception {
        CustomerCreationRequest request = CustomerCreationRequest.builder()
                .customerCode("C001")
                .fullName("Nguyen Van A")
                .build();

        when(customerService.createCustomer(any(CustomerCreationRequest.class)))
                .thenThrow(new DuplicateCustomerException("Mã khách hàng đã tồn tại: C001"));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1012))
                .andExpect(jsonPath("$.message").value("Mã khách hàng đã tồn tại: C001"));
    }
}
