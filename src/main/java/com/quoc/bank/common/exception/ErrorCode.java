package com.quoc.bank.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    UNCATEGORIZED_EXCEPTION(8888, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(9999, "Invalid message key", HttpStatus.BAD_REQUEST),

    // Customer errors (101x)
    CUSTOMER_NOT_FOUND(1011, "Không tìm thấy khách hàng", HttpStatus.NOT_FOUND),
    CUSTOMER_EXISTED(1012, "Mã khách hàng đã tồn tại", HttpStatus.BAD_REQUEST),
    CUSTOMER_HAS_ACCOUNTS(1013, "Không thể xóa khách hàng đang sở hữu tài khoản hoạt động hoặc có giao dịch", HttpStatus.BAD_REQUEST),

    // Account errors (102x)
    ACCOUNT_NOT_FOUND(1021, "Không tìm thấy tài khoản", HttpStatus.NOT_FOUND),
    ACCOUNT_EXISTED(1022, "Số tài khoản đã tồn tại", HttpStatus.BAD_REQUEST),
    INVALID_INITIAL_BALANCE(1023, "Số dư ban đầu không được âm", HttpStatus.BAD_REQUEST),
    INVALID_DAILY_LIMIT(1024, "Hạn mức giao dịch trong ngày phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    INVALID_ACCOUNT_TYPE(1025, "Loại tài khoản không hợp lệ (chỉ hỗ trợ PAYMENT hoặc SAVING)", HttpStatus.BAD_REQUEST),

    // Transaction errors (103x)
    INSUFFICIENT_BALANCE(1031, "Số dư tài khoản nguồn không đủ để thực hiện giao dịch", HttpStatus.BAD_REQUEST),
    TRANSACTION_LIMIT_EXCEEDED(1032, "Tổng tiền chuyển trong ngày vượt quá hạn mức giao dịch", HttpStatus.BAD_REQUEST),
    SELF_TRANSFER_NOT_ALLOWED(1033, "Không thể chuyển tiền cho chính tài khoản nguồn", HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT(1034, "Số tiền giao dịch phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    TRANSACTION_NOT_FOUND(1035, "Không tìm thấy giao dịch", HttpStatus.NOT_FOUND),

    // Validation errors (104x)
    VALIDATION_ERROR(1040, "Dữ liệu yêu cầu không hợp lệ: {field}", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
