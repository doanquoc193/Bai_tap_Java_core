package com.quoc.bank.transaction.controller;

import com.quoc.bank.common.response.ApiResponse;
import com.quoc.bank.transaction.dto.TransactionResponse;
import com.quoc.bank.transaction.dto.TransferRequest;
import com.quoc.bank.transaction.entity.TransactionStatus;
import com.quoc.bank.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionController {

    TransactionService transactionService;

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("REST: Transfer request: from={}, to={}, amount={}",
                request.getFromAccountNumber(), request.getToAccountNumber(), request.getAmount());
        return ApiResponse.success("Chuyển tiền thành công", transactionService.transfer(request));
    }

    @GetMapping
    public ApiResponse<Page<TransactionResponse>> getTransactions(
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ApiResponse.success("Lấy danh sách giao dịch thành công",
                transactionService.getTransactions(status, fromDate, toDate, pageable));
    }

    @GetMapping("/{referenceNumber}")
    public ApiResponse<TransactionResponse> getTransactionByRefNo(@PathVariable String referenceNumber) {
        return ApiResponse.success("Lấy thông tin giao dịch thành công",
                transactionService.getTransactionByRefNo(referenceNumber));
    }

    @GetMapping("/account/{accountNumber}")
    public ApiResponse<Page<TransactionResponse>> getTransactionsByAccount(
            @PathVariable String accountNumber,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ApiResponse.success("Lấy lịch sử giao dịch của tài khoản thành công",
                transactionService.getTransactionsByAccount(accountNumber, pageable));
    }

    @GetMapping("/analytics/total-success-amount")
    public ApiResponse<BigDecimal> getTotalSuccessAmount() {
        return ApiResponse.success("Tính tổng số tiền chuyển thành công",
                transactionService.getTotalSuccessAmount());
    }

    @GetMapping("/analytics/largest-success")
    public ApiResponse<TransactionResponse> getLargestSuccessTransaction() {
        return ApiResponse.success("Tìm giao dịch thành công lớn nhất",
                transactionService.getLargestSuccessTransaction());
    }

    @GetMapping("/analytics/grouped-by-account")
    public ApiResponse<Map<String, List<TransactionResponse>>> groupSuccessTransactionsByFromAccount() {
        return ApiResponse.success("Gom nhóm giao dịch thành công theo tài khoản nguồn",
                transactionService.groupSuccessTransactionsByFromAccount());
    }
}
