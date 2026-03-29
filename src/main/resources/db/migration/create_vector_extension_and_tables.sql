CREATE EXTENSION IF NOT EXISTS vector;

-- Tabela de embeddings de ordens
CREATE TABLE order_embeddings (
                                  id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                  order_id      VARCHAR(255) NOT NULL UNIQUE,
                                  user_id       VARCHAR(255) NOT NULL,
                                  order_type    VARCHAR(10)  NOT NULL,
                                  price         NUMERIC      NOT NULL,
                                  quantity      NUMERIC      NOT NULL,
                                  status        VARCHAR(30)  NOT NULL,
                                  content       TEXT         NOT NULL,
                                  embedding     vector(1536) NOT NULL,
                                  occurred_at   TIMESTAMP    NOT NULL
);

-- Tabela de embeddings de trades
CREATE TABLE trade_embeddings (
                                  id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                                  trade_id      VARCHAR(255) NOT NULL UNIQUE,
                                  buyer_id      VARCHAR(255) NOT NULL,
                                  seller_id     VARCHAR(255) NOT NULL,
                                  price         NUMERIC      NOT NULL,
                                  quantity      NUMERIC      NOT NULL,
                                  total_value   NUMERIC      NOT NULL,
                                  content       TEXT         NOT NULL,
                                  embedding     vector(1536) NOT NULL,
                                  occurred_at   TIMESTAMP    NOT NULL
);

-- Índices HNSW para busca vetorial eficiente por similaridade de cosseno
CREATE INDEX idx_order_embedding_hnsw
    ON order_embeddings USING hnsw (embedding vector_cosine_ops);

CREATE INDEX idx_trade_embedding_hnsw
    ON trade_embeddings USING hnsw (embedding vector_cosine_ops);

-- Índices auxiliares para filtros por usuário
CREATE INDEX idx_order_embeddings_user_id ON order_embeddings (user_id);
CREATE INDEX idx_trade_embeddings_buyer_id ON trade_embeddings (buyer_id);
CREATE INDEX idx_trade_embeddings_seller_id ON trade_embeddings (seller_id);