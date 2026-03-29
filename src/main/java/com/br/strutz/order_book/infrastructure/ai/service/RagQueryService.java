//package com.br.strutz.order_book.infrastructure.ai.service;
//
//import com.br.strutz.order_book.domain.port.output.persistence.entities.OrderEmbeddingEntity;
//import com.br.strutz.order_book.domain.port.output.persistence.entities.TradeEmbeddingEntity;
//import com.br.strutz.order_book.domain.port.output.persistence.repository.OrderEmbeddingRepository;
//import com.br.strutz.order_book.domain.port.output.persistence.repository.TradeEmbeddingRepository;
//import com.br.strutz.order_book.domain.service.ai.EmbeddingService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.ai.anthropic.AnthropicChatModel;
//import org.springframework.ai.chat.messages.SystemMessage;
//import org.springframework.ai.chat.messages.UserMessage;
//import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class RagQueryService {
//
//    private static final Logger log =
//            LoggerFactory.getLogger(RagQueryService.class);
//
//    private static final int TOP_K = 5;
//
//    private static final String SYSTEM_PROMPT = """
//            You are a financial assistant specialized in cryptocurrency trading analysis
//            for the VBR/BRL order book.
//
//            You will receive context about recent orders and trades executed in the system.
//            Use this context to answer the user's question accurately and concisely.
//
//            Rules:
//            - Base your answers strictly on the provided context.
//            - If the context is insufficient, say so clearly.
//            - Always respond in the same language as the user's question.
//            - Format numbers clearly (e.g., prices in BRL, quantities in VBR).
//            """;
//
//    private final EmbeddingService embeddingService;
//    private final OrderEmbeddingRepository orderEmbeddingRepository;
//    private final TradeEmbeddingRepository tradeEmbeddingRepository;
//    private final AnthropicChatModel chatModel;
//
//    public RagQueryService(EmbeddingService embeddingService,
//                           OrderEmbeddingRepository orderEmbeddingRepository,
//                           TradeEmbeddingRepository tradeEmbeddingRepository,
//                           AnthropicChatModel chatModel) {
//        this.embeddingService         = embeddingService;
//        this.orderEmbeddingRepository = orderEmbeddingRepository;
//        this.tradeEmbeddingRepository = tradeEmbeddingRepository;
//        this.chatModel                = chatModel;
//    }
//
//    // Busca semântica global — sem filtro de usuário
//    public String query(String userQuestion) {
//        log.info("RAG query — question_length={}", userQuestion.length());
//
//        float[] queryEmbedding   = embeddingService.generateEmbedding(userQuestion);
//        String  vectorString     = embeddingService.toVectorString(queryEmbedding);
//
//        List<OrderEmbeddingEntity> similarOrders = orderEmbeddingRepository
//                .findTopKSimilar(vectorString, TOP_K);
//
//        List<TradeEmbeddingEntity> similarTrades = tradeEmbeddingRepository
//                .findTopKSimilar(vectorString, TOP_K);
//
//        String context = buildContext(similarOrders, similarTrades);
//        String answer  = callClaude(userQuestion, context);
//
//        log.info("RAG query completed — orders_found={} trades_found={}",
//                similarOrders.size(), similarTrades.size());
//
//        return answer;
//    }
//
//    // Busca semântica filtrada por usuário
//    public String queryByUser(String userQuestion, String userId) {
//        log.info("RAG query — userId={} question_length={}", userId, userQuestion.length());
//
//        float[] queryEmbedding   = embeddingService.generateEmbedding(userQuestion);
//        String  vectorString     = embeddingService.toVectorString(queryEmbedding);
//
//        List<OrderEmbeddingEntity> similarOrders = orderEmbeddingRepository
//                .findTopKSimilarByUserId(vectorString, userId, TOP_K);
//
//        List<TradeEmbeddingEntity> similarTrades = tradeEmbeddingRepository
//                .findTopKSimilarByUserId(vectorString, userId, TOP_K);
//
//        String context = buildContext(similarOrders, similarTrades);
//        String answer  = callClaude(userQuestion, context);
//
//        log.info("RAG query by user completed — userId={} orders_found={} trades_found={}",
//                userId, similarOrders.size(), similarTrades.size());
//
//        return answer;
//    }
//
//    // -------------------------------------------------------------------------
//    // Private
//    // -------------------------------------------------------------------------
//
//    private String callClaude(String userQuestion, String context) {
//        String contextualQuestion = """
//                Context from the order book:
//                %s
//
//                User question:
//                %s
//                """.formatted(context, userQuestion);
//
//        Prompt prompt = new Prompt(List.of(
//                new SystemMessage(SYSTEM_PROMPT),
//                new UserMessage(contextualQuestion)
//        ));
//
//        return chatModel.call(prompt)
//                .getResult()
//                .getOutput()
//                .getContent();
//    }
//
//    private String buildContext(List<OrderEmbeddingEntity> orders,
//                                List<TradeEmbeddingEntity> trades) {
//        StringBuilder sb = new StringBuilder();
//
//        if (!orders.isEmpty()) {
//            sb.append("=== Recent Similar Orders ===\n");
//            orders.forEach(o -> sb.append("""
//                    - Order %s | User: %s | Type: %s | Price: %s BRL | Qty: %s VBR | Status: %s | At: %s
//                    """.formatted(
//                    o.getOrderId(),
//                    o.getUserId(),
//                    o.getOrderType(),
//                    o.getPrice(),
//                    o.getQuantity(),
//                    o.getStatus(),
//                    o.getOccurredAt()
//            )));
//        }
//
//        if (!trades.isEmpty()) {
//            sb.append("\n=== Recent Similar Trades ===\n");
//            trades.forEach(t -> sb.append("""
//                    - Trade %s | Buyer: %s | Seller: %s | Price: %s BRL | Qty: %s VBR | Total: %s BRL | At: %s
//                    """.formatted(
//                    t.getTradeId(),
//                    t.getBuyerId(),
//                    t.getSellerId(),
//                    t.getPrice(),
//                    t.getQuantity(),
//                    t.getTotalValue(),
//                    t.getOccurredAt()
//            )));
//        }
//
//        if (sb.isEmpty()) {
//            return "No relevant orders or trades found in the context.";
//        }
//
//        return sb.toString();
//    }
//}