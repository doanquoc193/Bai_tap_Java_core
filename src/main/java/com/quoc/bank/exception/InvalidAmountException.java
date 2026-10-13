package com.quoc.bank.exception;

import com.quoc.bank.common.exception.AppException;
import com.quoc.bank.common.exception.ErrorCode;

public class InvalidAmountException extends AppException {

    public InvalidAmountException(String message) {
        super(ErrorCode.INVALID_AMOUNT, message);
    }

    public InvalidAmountException() {
        super(ErrorCode.INVALID_AMOUNT);
    }
}