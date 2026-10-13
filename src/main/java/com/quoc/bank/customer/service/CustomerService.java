package com.quoc.bank.customer.service;

import com.quoc.bank.account.repository.AccountRepository;
import com.quoc.bank.common.exception.AppException;
import com.quoc.bank.common.exception.ErrorCode;
import com.quoc.bank.customer.dto.CustomerCreationRequest;
import com.quoc.bank.customer.dto.CustomerResponse;
import com.quoc.bank.customer.dto.CustomerUpdateRequest;
import com.quoc.bank.customer.entity.Customer;
import com.quoc.bank.customer.repository.CustomerRepository;
import com.quoc.bank.exception.CustomerNotFoundException;
import com.quoc.bank.exception.DuplicateCustomerException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerService {

    CustomerRepository customerRepository;
    AccountRepository accountRepository;

    @Transactional
    public CustomerResponse createCustomer(CustomerCreationRequest request) {
        if (customerRepository.existsByCustomerCode(request.getCustomerCode())) {
            throw new DuplicateCustomerException("Mã khách hàng đã tồn tại: " + request.getCustomerCode());
        }

        Customer customer = Customer.builder()
                .customerCode(request.getCustomerCode().trim())
                .fullName(request.getFullName().trim())
                .email(request.getEmail() != null ? request.getEmail().trim() : null)
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null)
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Created customer successfully: code={}, id={}", savedCustomer.getCustomerCode(), savedCustomer.getId());
        return toCustomerResponse(savedCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Không tìm thấy khách hàng với ID: " + id));
        return toCustomerResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByCode(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new CustomerNotFoundException("Không tìm thấy khách hàng với mã: " + customerCode));
        return toCustomerResponse(customer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> getCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable)
                .map(this::toCustomerResponse);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toCustomerResponse)
                .toList();
    }

    @Transactional
    public CustomerResponse updateCustomer(UUID id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Không tìm thấy khách hàng với ID: " + id));

        customer.setFullName(request.getFullName().trim());
        if (request.getEmail() != null) {
            customer.setEmail(request.getEmail().trim());
        }
        if (request.getPhoneNumber() != null) {
            customer.setPhoneNumber(request.getPhoneNumber().trim());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Updated customer successfully: id={}", id);
        return toCustomerResponse(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Không tìm thấy khách hàng với ID: " + id));

        long accountCount = accountRepository.countByOwnerId(id);
        if (accountCount > 0) {
            throw new AppException(ErrorCode.CUSTOMER_HAS_ACCOUNTS,
                    "Không thể xóa khách hàng đang sở hữu " + accountCount + " tài khoản");
        }

        customerRepository.delete(customer);
        log.info("Deleted customer successfully: id={}", id);
    }

    public CustomerResponse toCustomerResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerCode(customer.getCustomerCode())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .accountCount(customer.getAccounts() != null ? customer.getAccounts().size() : 0)
                .build();
    }
}
