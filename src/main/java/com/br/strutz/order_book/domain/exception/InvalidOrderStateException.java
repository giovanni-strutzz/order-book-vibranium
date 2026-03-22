package com.br.strutz.order_book.domain.exception;

public class InvalidOrderStateException extends DomainException {
    public InvalidOrderStateException(String msg) { super(msg); }
}