package com.br.strutz.order_book.adapter.output.kafka.exception;

public class EventPublishException extends RuntimeException {

    public EventPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}