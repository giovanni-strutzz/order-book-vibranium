package com.br.strutz.order_book.domain.exception;

public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) { super(message); }
};