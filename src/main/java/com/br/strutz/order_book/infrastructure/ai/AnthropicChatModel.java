package com.br.strutz.order_book.infrastructure.ai;

import com.br.strutz.order_book.adapter.input.rest.dto.request.AnthropicRequest;
import org.springframework.ai.chat.model.ChatResponse;
import com.br.strutz.order_book.domain.ai.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AnthropicChatModel {

    private final WebClient webClient;
    private final String apiKey;

    public AnthropicChatModel(@Value("${anthropic.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.anthropic.com/v1/messages")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public ChatResponse call(Prompt prompt) {
        AnthropicRequest request = new AnthropicRequest(
                "claude-3-5-sonnet-20241022",
                prompt.toMessages(),
                1024
        );

        return webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatResponse.class)
                .block();
    }
}
