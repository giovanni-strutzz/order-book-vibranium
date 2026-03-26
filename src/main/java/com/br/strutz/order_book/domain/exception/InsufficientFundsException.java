package com.br.strutz.order_book.domain.exception;

public class InsufficientFundsException extends DomainException {
    public InsufficientFundsException(String msg) { super(msg); }
}