package com.quoc.bank.exception;

import com.quoc.bank.common.exception.AppException;
import com.quoc.bank.common.exception.ErrorCode;

public class InsufficientBalanceException extends AppException {

    public InsufficientBalanceException(String message) {
        super(ErrorCode.INSUFFICIENT_BALANCE, message);
    }

    public InsufficientBalanceException() {
        super(ErrorCode.INSUFFICIENT_BALANCE);
    }
}