package com.quoc.bank.exception;

import com.quoc.bank.common.exception.AppException;
import com.quoc.bank.common.exception.ErrorCode;

public class TransactionLimitExceededException extends AppException {

    public TransactionLimitExceededException(String message) {
        super(ErrorCode.TRANSACTION_LIMIT_EXCEEDED, message);
    }

    public TransactionLimitExceededException() {
        super(ErrorCode.TRANSACTION_LIMIT_EXCEEDED);
    }
}