package com.quoc.bank.exception;

import com.quoc.bank.common.exception.AppException;
import com.quoc.bank.common.exception.ErrorCode;

public class InvalidTransactionException extends AppException {

    public InvalidTransactionException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }

    public InvalidTransactionException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}