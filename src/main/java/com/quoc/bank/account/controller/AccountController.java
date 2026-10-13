package com.quoc.bank.account.controller;

import com.quoc.bank.account.dto.AccountCreationRequest;
import com.quoc.bank.account.dto.AccountDepositRequest;
import com.quoc.bank.account.dto.AccountResponse;
import com.quoc.bank.account.dto.BalanceResponse;
import com.quoc.bank.account.service.AccountService;
import com.quoc.bank.common.response.ApiResponse;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountController {

    AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AccountResponse> createAccount(@Valid @RequestBody AccountCreationRequest request) {
        log.info("REST: Create account request: {}", request.getAccountNumber());
        return ApiResponse.success("Tạo tài khoản thành công", accountService.createAccount(request));
    }

    @GetMapping
    public ApiResponse<Page<AccountResponse>> getAccounts(@PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success("Lấy danh sách tài khoản thành công", accountService.getAccounts(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<AccountResponse> getAccountById(@PathVariable UUID id) {
        return ApiResponse.success("Lấy thông tin tài khoản thành công", accountService.getAccountById(id));
    }

    @GetMapping("/number/{accountNumber}")
    public ApiResponse<AccountResponse> getAccountByNumber(@PathVariable String accountNumber) {
        return ApiResponse.success("Lấy thông tin tài khoản thành công", accountService.getAccountByNumber(accountNumber));
    }

    @GetMapping("/{accountNumber}/balance")
    public ApiResponse<BalanceResponse> getBalance(@PathVariable String accountNumber) {
        return ApiResponse.success("Kiểm tra số dư thành công", accountService.getBalance(accountNumber));
    }

    @GetMapping("/customer/{customerId}")
    public ApiResponse<List<AccountResponse>> getAccountsByCustomerId(@PathVariable UUID customerId) {
        return ApiResponse.success("Lấy danh sách tài khoản của khách hàng thành công",
                accountService.getAccountsByCustomerId(customerId));
    }

    @PostMapping("/{accountNumber}/deposits")
    public ApiResponse<AccountResponse> deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody AccountDepositRequest request
    ) {
        log.info("REST: Deposit request: account={}, amount={}", accountNumber, request.getAmount());
        return ApiResponse.success("Nạp tiền thành công", accountService.deposit(accountNumber, request.getAmount()));
    }
}
