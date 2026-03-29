package com.br.strutz.order_book.adapter.input.rest.dto.request;

public record ChatMessage(String role, String content) {
    public static ChatMessage system(String content) {
        return new ChatMessage("system", content);
    }
    public static ChatMessage user(String content) {
        return new ChatMessage("user", content);
    }
}