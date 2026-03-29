//package com.br.strutz.order_book.domain.service.ai;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.ai.embedding.EmbeddingModel;
//import org.springframework.ai.embedding.EmbeddingRequest;
//import org.springframework.ai.embedding.EmbeddingResponse;
//import org.springframework.ai.openai.OpenAiEmbeddingOptions;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class EmbeddingService {
//
//    private static final Logger log =
//            LoggerFactory.getLogger(EmbeddingService.class);
//
//    private static final String EMBEDDING_MODEL = "text-embedding-3-small";
//    private static final int    EMBEDDING_DIM   = 1536;
//
//    private final EmbeddingModel embeddingModel;
//
//    public EmbeddingService(EmbeddingModel embeddingModel) {
//        this.embeddingModel = embeddingModel;
//    }
//
//    public float[] generateEmbedding(String text) {
//        log.debug("Generating embedding — text_length={}", text.length());
//
//        EmbeddingRequest request = new EmbeddingRequest(
//                List.of(text),
//                OpenAiEmbeddingOptions.builder()
//                        .withModel(EMBEDDING_MODEL)
//                        .build()
//        );
//
//        EmbeddingResponse response = embeddingModel.call(request);
//
//        float[] embedding = toFloatArray(
//                response.getResults().getFirst().getOutput()
//        );
//
//        log.debug("Embedding generated — dim={}", embedding.length);
//        return embedding;
//    }
//
//    public String toVectorString(float[] embedding) {
//        StringBuilder sb = new StringBuilder("[");
//        for (int i = 0; i < embedding.length; i++) {
//            sb.append(embedding[i]);
//            if (i < embedding.length - 1) sb.append(",");
//        }
//        sb.append("]");
//        return sb.toString();
//    }
//
//    private float[] toFloatArray(float @org.checkerframework.checker.nullness.qual.MonotonicNonNull [] doubles) {
//        float[] floats = new float[doubles.size()];
//        for (int i = 0; i < doubles.size(); i++) {
//            floats[i] = doubles.get(i).floatValue();
//        }
//        return floats;
//    }
//}
