package com.br.strutz.order_book.adapter.input.rest.dto.request;

import com.anthropic.models.Message;

import java.util.List;

public record AnthropicRequest(
        String model,
        List<Message> messages,
        int max_tokens
) {}