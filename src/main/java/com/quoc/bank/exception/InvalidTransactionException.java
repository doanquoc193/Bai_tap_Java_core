package com.quoc.bank.exception;
//xu li nghiep vu nhu :
//chuyển cho chính mình;
//amount bằng 0;
//amount âm;
//dữ liệu giao dịch không hợp lệ.
public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}