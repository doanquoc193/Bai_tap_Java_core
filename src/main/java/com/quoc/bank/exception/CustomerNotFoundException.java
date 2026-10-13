package com.quoc.bank.exception;

import com.quoc.bank.common.exception.AppException;
import com.quoc.bank.common.exception.ErrorCode;

public class CustomerNotFoundException extends AppException {

    public CustomerNotFoundException(String message) {
        super(ErrorCode.CUSTOMER_NOT_FOUND, message);
    }

    public CustomerNotFoundException() {
        super(ErrorCode.CUSTOMER_NOT_FOUND);
    }
}