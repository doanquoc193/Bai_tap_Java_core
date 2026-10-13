package com.quoc.bank.exception;

import com.quoc.bank.common.exception.AppException;
import com.quoc.bank.common.exception.ErrorCode;

public class DuplicateAccountException extends AppException {

    public DuplicateAccountException(String message) {
        super(ErrorCode.ACCOUNT_EXISTED, message);
    }

    public DuplicateAccountException() {
        super(ErrorCode.ACCOUNT_EXISTED);
    }
}