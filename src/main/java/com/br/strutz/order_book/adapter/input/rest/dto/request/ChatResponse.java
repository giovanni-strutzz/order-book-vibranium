package com.br.strutz.order_book.adapter.input.rest.dto.request;

import java.util.List;

public record ChatResponse(
        List<ChatContent> content
) {}