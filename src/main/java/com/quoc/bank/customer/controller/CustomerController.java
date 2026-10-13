package com.quoc.bank.customer.controller;

import com.quoc.bank.common.response.ApiResponse;
import com.quoc.bank.customer.dto.CustomerCreationRequest;
import com.quoc.bank.customer.dto.CustomerResponse;
import com.quoc.bank.customer.dto.CustomerUpdateRequest;
import com.quoc.bank.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerController {

    CustomerService customerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreationRequest request) {
        log.info("REST: Create customer request: {}", request.getCustomerCode());
        return ApiResponse.success("Tạo khách hàng thành công", customerService.createCustomer(request));
    }

    @GetMapping
    public ApiResponse<Page<CustomerResponse>> getCustomers(@PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success("Lấy danh sách khách hàng thành công", customerService.getCustomers(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerResponse> getCustomerById(@PathVariable UUID id) {
        return ApiResponse.success("Lấy thông tin khách hàng thành công", customerService.getCustomerById(id));
    }

    @GetMapping("/code/{customerCode}")
    public ApiResponse<CustomerResponse> getCustomerByCode(@PathVariable String customerCode) {
        return ApiResponse.success("Lấy thông tin khách hàng thành công", customerService.getCustomerByCode(customerCode));
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerResponse> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerUpdateRequest request
    ) {
        log.info("REST: Update customer request: id={}", id);
        return ApiResponse.success("Cập nhật thông tin khách hàng thành công", customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCustomer(@PathVariable UUID id) {
        log.info("REST: Delete customer request: id={}", id);
        customerService.deleteCustomer(id);
        return ApiResponse.success("Xóa khách hàng thành công", null);
    }
}
