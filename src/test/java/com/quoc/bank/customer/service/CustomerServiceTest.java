package com.quoc.bank.customer.service;

import com.quoc.bank.account.repository.AccountRepository;
import com.quoc.bank.common.exception.AppException;
import com.quoc.bank.customer.dto.CustomerCreationRequest;
import com.quoc.bank.customer.dto.CustomerResponse;
import com.quoc.bank.customer.dto.CustomerUpdateRequest;
import com.quoc.bank.customer.entity.Customer;
import com.quoc.bank.customer.repository.CustomerRepository;
import com.quoc.bank.exception.CustomerNotFoundException;
import com.quoc.bank.exception.DuplicateCustomerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerCreationRequest creationRequest;

    @BeforeEach
    void setUp() {
        UUID customerId = UUID.randomUUID();
        customer = Customer.builder()
                .id(customerId)
                .customerCode("CUST-001")
                .fullName("Nguyen Van A")
                .email("vana@example.com")
                .phoneNumber("0901234567")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .accounts(new ArrayList<>())
                .build();

        creationRequest = CustomerCreationRequest.builder()
                .customerCode("CUST-001")
                .fullName("Nguyen Van A")
                .email("vana@example.com")
                .phoneNumber("0901234567")
                .build();
    }

    @Test
    @DisplayName("Tạo khách hàng thành công")
    void createCustomer_success() {
        when(customerRepository.existsByCustomerCode("CUST-001")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerResponse response = customerService.createCustomer(creationRequest);

        assertNotNull(response);
        assertEquals("CUST-001", response.getCustomerCode());
        assertEquals("Nguyen Van A", response.getFullName());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Tạo khách hàng với mã bị trùng - ném DuplicateCustomerException")
    void createCustomer_duplicateCode_throwsException() {
        when(customerRepository.existsByCustomerCode("CUST-001")).thenReturn(true);

        assertThrows(DuplicateCustomerException.class, () -> customerService.createCustomer(creationRequest));
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Tìm khách hàng theo ID thành công")
    void getCustomerById_success() {
        UUID id = customer.getId();
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.getCustomerById(id);

        assertNotNull(response);
        assertEquals(id, response.getId());
    }

    @Test
    @DisplayName("Tìm khách hàng không tồn tại - ném CustomerNotFoundException")
    void getCustomerById_notFound() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomerById(id));
    }

    @Test
    @DisplayName("Cập nhật thông tin khách hàng thành công")
    void updateCustomer_success() {
        UUID id = customer.getId();
        CustomerUpdateRequest updateRequest = CustomerUpdateRequest.builder()
                .fullName("Nguyen Van B")
                .email("vanb@example.com")
                .phoneNumber("0909999999")
                .build();

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerResponse response = customerService.updateCustomer(id, updateRequest);

        assertNotNull(response);
        assertEquals("Nguyen Van B", customer.getFullName());
    }

    @Test
    @DisplayName("Xóa khách hàng có tài khoản liên kết - ném AppException")
    void deleteCustomer_hasAccounts_throwsException() {
        UUID id = customer.getId();
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        when(accountRepository.countByOwnerId(id)).thenReturn(2L);

        assertThrows(AppException.class, () -> customerService.deleteCustomer(id));
        verify(customerRepository, never()).delete(any());
    }
}
