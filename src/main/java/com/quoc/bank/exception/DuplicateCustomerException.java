package com.quoc.bank.exception;

import com.quoc.bank.common.exception.AppException;
import com.quoc.bank.common.exception.ErrorCode;

public class DuplicateCustomerException extends AppException {

    public DuplicateCustomerException(String message) {
        super(ErrorCode.CUSTOMER_EXISTED, message);
    }

    public DuplicateCustomerException() {
        super(ErrorCode.CUSTOMER_EXISTED);
    }
}