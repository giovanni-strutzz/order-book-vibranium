package com.br.strutz.order_book.domain.ai;


import com.anthropic.models.ContentBlock;
import com.anthropic.models.Message;
import com.br.strutz.order_book.adapter.input.rest.dto.request.ChatMessage;

import java.util.List;

public class Prompt {
    private final List<ChatMessage> chatMessages;

    public Prompt(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public List<Message> toMessages() {
        return chatMessages.stream()
                .map(m -> Message.builder()
                        .role("system".equalsIgnoreCase(m.role())
                                ? Message.Role.of("SYSTEM")
                                : Message.Role.of("USER"))
                        .content(List.of(ContentBlock.text(m.content())))
                        .build()
                )
                .toList();
    }
}