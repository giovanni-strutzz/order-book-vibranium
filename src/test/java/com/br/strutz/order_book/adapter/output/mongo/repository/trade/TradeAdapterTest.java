package com.br.strutz.order_book.adapter.output.mongo.repository.trade;

import com.br.strutz.order_book.adapter.output.mongo.adapter.TradeAdapter;
import com.br.strutz.order_book.adapter.output.mongo.document.TradeDocument;
import com.br.strutz.order_book.adapter.output.mongo.mapper.TradeMapper;
import com.br.strutz.order_book.domain.model.TradeId;
import com.br.strutz.order_book.domain.model.aggregates.Trade;
import com.br.strutz.order_book.domain.model.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeAdapter")
class TradeAdapterTest {

    @Mock
    private TradeMongoRepository mongoRepository;

    @Mock
    private TradeMapper mapper;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private TradeAdapter tradeAdapter;

    

    private TradeDocument tradeDocument;
    private Trade tradeDomain;
    private TradeId tradeId;
    private UserId userId;

    @BeforeEach
    void setUp() {
        String rawId  = UUID.randomUUID().toString();
        String rawUid = UUID.randomUUID().toString();

        tradeId       = mock(TradeId.class);
        userId        = mock(UserId.class);
        tradeDocument = mock(TradeDocument.class);
        tradeDomain   = mock(Trade.class);

        lenient().when(tradeId.getValue()).thenReturn(rawId);
        lenient().when(userId.getValue()).thenReturn(rawUid);
    }

    

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("deve converter para documento, persistir e retornar domínio")
        void shouldConvertPersistAndReturnDomain() {
            when(mapper.toDocument(tradeDomain)).thenReturn(tradeDocument);
            when(mongoRepository.save(tradeDocument)).thenReturn(tradeDocument);
            when(mapper.toDomain(tradeDocument)).thenReturn(tradeDomain);

            Trade result = tradeAdapter.save(tradeDomain);

            assertThat(result).isSameAs(tradeDomain);
            verify(mapper).toDocument(tradeDomain);
            verify(mongoRepository).save(tradeDocument);
            verify(mapper).toDomain(tradeDocument);
        }

        @Test
        @DisplayName("deve propagar exceção lançada pelo repositório")
        void shouldPropagateRepositoryException() {
            when(mapper.toDocument(tradeDomain)).thenReturn(tradeDocument);
            when(mongoRepository.save(any())).thenThrow(new RuntimeException("DB error"));

            org.junit.jupiter.api.Assertions.assertThrows(
                    RuntimeException.class,
                    () -> tradeAdapter.save(tradeDomain)
            );
        }
    }

    

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("deve retornar Optional com domínio quando documento existe")
        void shouldReturnPresentOptionalWhenFound() {
            when(mongoRepository.findById(tradeId.getValue()))
                    .thenReturn(Optional.of(tradeDocument));
            when(mapper.toDomain(tradeDocument)).thenReturn(tradeDomain);

            Optional<Trade> result = tradeAdapter.findById(tradeId);

            assertThat(result).isPresent().containsSame(tradeDomain);
            verify(mongoRepository).findById(tradeId.getValue());
            verify(mapper).toDomain(tradeDocument);
        }

        @Test
        @DisplayName("deve retornar Optional vazio quando documento não existe")
        void shouldReturnEmptyOptionalWhenNotFound() {
            when(mongoRepository.findById(tradeId.getValue()))
                    .thenReturn(Optional.empty());

            Optional<Trade> result = tradeAdapter.findById(tradeId);

            assertThat(result).isEmpty();
            verify(mapper, never()).toDomain(any());
        }
    }

    

    @Nested
    @DisplayName("findByUserIdOrderByExecutedAtDesc()")
    class FindByUserId {

        @Test
        @DisplayName("deve construir query com orOperator e retornar lista mapeada")
        void shouldBuildQueryAndReturnMappedList() {
            when(mongoTemplate.find(any(Query.class), eq(TradeDocument.class)))
                    .thenReturn(List.of(tradeDocument));
            when(mapper.toDomain(tradeDocument)).thenReturn(tradeDomain);

            List<Trade> result = tradeAdapter.findByUserIdOrderByExecutedAtDesc(userId, 0, 10);

            assertThat(result).hasSize(1).containsExactly(tradeDomain);
            verify(mongoTemplate).find(any(Query.class), eq(TradeDocument.class));
            verify(mapper).toDomain(tradeDocument);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há documentos")
        void shouldReturnEmptyListWhenNoDocumentsFound() {
            when(mongoTemplate.find(any(Query.class), eq(TradeDocument.class)))
                    .thenReturn(Collections.emptyList());

            List<Trade> result = tradeAdapter.findByUserIdOrderByExecutedAtDesc(userId, 0, 10);

            assertThat(result).isEmpty();
            verify(mapper, never()).toDomain(any());
        }

        @Test
        @DisplayName("deve passar os parâmetros de paginação corretamente na query")
        void shouldPassPaginationParametersToQuery() {
            ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
            when(mongoTemplate.find(queryCaptor.capture(), eq(TradeDocument.class)))
                    .thenReturn(Collections.emptyList());

            tradeAdapter.findByUserIdOrderByExecutedAtDesc(userId, 2, 5);

            Query capturedQuery = queryCaptor.getValue();
            
            assertThat(capturedQuery.getSkip()).isEqualTo(10L);
            assertThat(capturedQuery.getLimit()).isEqualTo(5);
        }

        @Test
        @DisplayName("deve mapear múltiplos documentos retornados")
        void shouldMapMultipleDocuments() {
            TradeDocument doc2   = mock(TradeDocument.class);
            Trade         trade2 = mock(Trade.class);

            when(mongoTemplate.find(any(Query.class), eq(TradeDocument.class)))
                    .thenReturn(List.of(tradeDocument, doc2));
            when(mapper.toDomain(tradeDocument)).thenReturn(tradeDomain);
            when(mapper.toDomain(doc2)).thenReturn(trade2);

            List<Trade> result = tradeAdapter.findByUserIdOrderByExecutedAtDesc(userId, 0, 10);

            assertThat(result).hasSize(2).containsExactly(tradeDomain, trade2);
        }
    }

    

    @Nested
    @DisplayName("findByExecutedAtBetween()")
    class FindByExecutedAtBetween {

        private final Instant from = Instant.parse("2024-01-01T00:00:00Z");
        private final Instant to   = Instant.parse("2024-01-31T23:59:59Z");

        @Test
        @DisplayName("deve delegar ao repositório e retornar lista mapeada")
        void shouldDelegateAndReturnMappedList() {
            when(mongoRepository.findByExecutedAtBetween(from, to))
                    .thenReturn(List.of(tradeDocument));
            when(mapper.toDomain(tradeDocument)).thenReturn(tradeDomain);

            List<Trade> result = tradeAdapter.findByExecutedAtBetween(from, to);

            assertThat(result).hasSize(1).containsExactly(tradeDomain);
            verify(mongoRepository).findByExecutedAtBetween(from, to);
            verify(mapper).toDomain(tradeDocument);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando repositório não encontra registros")
        void shouldReturnEmptyListWhenNoneFound() {
            when(mongoRepository.findByExecutedAtBetween(from, to))
                    .thenReturn(Collections.emptyList());

            List<Trade> result = tradeAdapter.findByExecutedAtBetween(from, to);

            assertThat(result).isEmpty();
            verify(mapper, never()).toDomain(any());
        }

        @Test
        @DisplayName("deve mapear múltiplos documentos no intervalo")
        void shouldMapMultipleDocumentsInRange() {
            TradeDocument doc2   = mock(TradeDocument.class);
            Trade         trade2 = mock(Trade.class);

            when(mongoRepository.findByExecutedAtBetween(from, to))
                    .thenReturn(List.of(tradeDocument, doc2));
            when(mapper.toDomain(tradeDocument)).thenReturn(tradeDomain);
            when(mapper.toDomain(doc2)).thenReturn(trade2);

            List<Trade> result = tradeAdapter.findByExecutedAtBetween(from, to);

            assertThat(result).hasSize(2).containsExactlyInAnyOrder(tradeDomain, trade2);
        }

        @Test
        @DisplayName("deve repassar os instantes exatos para o repositório")
        void shouldForwardExactInstantsToRepository() {
            when(mongoRepository.findByExecutedAtBetween(any(), any()))
                    .thenReturn(Collections.emptyList());

            tradeAdapter.findByExecutedAtBetween(from, to);

            verify(mongoRepository).findByExecutedAtBetween(from, to);
        }
    }
}