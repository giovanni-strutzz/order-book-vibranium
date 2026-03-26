package com.br.strutz.order_book.adapter.output.mongo.repository.order;

import com.br.strutz.order_book.adapter.output.mongo.document.OrderDocument;
import com.br.strutz.order_book.adapter.output.mongo.mapper.OrderMapper;
import com.br.strutz.order_book.domain.model.aggregates.Order;
import com.br.strutz.order_book.domain.model.order.OrderId;
import com.br.strutz.order_book.domain.model.order.OrderType;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderAdapter")
class OrderAdapterTest {

    @Mock
    private OrderMongoRepository mongoRepository;

    @Mock
    private OrderMapper mapper;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private OrderAdapter orderAdapter;

    private Order domainOrder;
    private OrderDocument orderDocument;
    private OrderId orderId;
    private UserId userId;

    @BeforeEach
    void setUp() {
        orderId = mock(OrderId.class);
        userId = mock(UserId.class);
        domainOrder = mock(Order.class);
        orderDocument = mock(OrderDocument.class);

        lenient().when(orderId.getValue()).thenReturn("order-123");
        lenient().when(userId.getValue()).thenReturn("user-456");
    }

    
    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("deve mapear, persistir e retornar o domínio salvo")
        void shouldMapPersistAndReturnDomain() {
            Order savedDomain = mock(Order.class);
            when(mapper.toDocument(domainOrder)).thenReturn(orderDocument);
            when(mongoRepository.save(orderDocument)).thenReturn(orderDocument);
            when(mapper.toDomain(orderDocument)).thenReturn(savedDomain);

            Order result = orderAdapter.save(domainOrder);

            assertThat(result).isSameAs(savedDomain);
            verify(mapper).toDocument(domainOrder);
            verify(mongoRepository).save(orderDocument);
            verify(mapper).toDomain(orderDocument);
        }
    }

    
    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("deve retornar Optional com domínio quando documento existe")
        void shouldReturnOrderWhenFound() {
            when(mongoRepository.findById("order-123"))
                    .thenReturn(Optional.of(orderDocument));
            when(mapper.toDomain(orderDocument)).thenReturn(domainOrder);

            Optional<Order> result = orderAdapter.findById(orderId);

            assertThat(result).isPresent().contains(domainOrder);
        }

        @Test
        @DisplayName("deve retornar Optional vazio quando documento não existe")
        void shouldReturnEmptyWhenNotFound() {
            when(mongoRepository.findById("order-123"))
                    .thenReturn(Optional.empty());

            Optional<Order> result = orderAdapter.findById(orderId);

            assertThat(result).isEmpty();
            verify(mapper, never()).toDomain(any());
        }
    }

    
    @Nested
    @DisplayName("findByCorrelationId()")
    class FindByCorrelationId {

        @Test
        @DisplayName("deve retornar Optional com domínio quando correlationId existe")
        void shouldReturnOrderWhenFound() {
            when(mongoRepository.findByCorrelationId("corr-001"))
                    .thenReturn(Optional.of(orderDocument));
            when(mapper.toDomain(orderDocument)).thenReturn(domainOrder);

            Optional<Order> result = orderAdapter.findByCorrelationId("corr-001");

            assertThat(result).isPresent().contains(domainOrder);
        }

        @Test
        @DisplayName("deve retornar Optional vazio quando correlationId não existe")
        void shouldReturnEmptyWhenNotFound() {
            when(mongoRepository.findByCorrelationId("corr-999"))
                    .thenReturn(Optional.empty());

            Optional<Order> result = orderAdapter.findByCorrelationId("corr-999");

            assertThat(result).isEmpty();
        }
    }

    
    @Nested
    @DisplayName("findActiveByType()")
    class FindActiveByType {

        @Test
        @DisplayName("deve retornar lista de ordens ativas do tipo informado")
        void shouldReturnActiveOrdersByType() {
            OrderType type = OrderType.BUY;
            OrderDocument doc1 = mock(OrderDocument.class);
            OrderDocument doc2 = mock(OrderDocument.class);
            Order order1 = mock(Order.class);
            Order order2 = mock(Order.class);

            when(mongoRepository.findByTypeAndStatusIn(
                    eq("BUY"), anyList()))
                    .thenReturn(List.of(doc1, doc2));
            when(mapper.toDomain(doc1)).thenReturn(order1);
            when(mapper.toDomain(doc2)).thenReturn(order2);

            List<Order> result = orderAdapter.findActiveByType(type);

            assertThat(result).hasSize(2).containsExactly(order1, order2);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há ordens ativas")
        void shouldReturnEmptyListWhenNoActiveOrders() {
            when(mongoRepository.findByTypeAndStatusIn(anyString(), anyList()))
                    .thenReturn(List.of());

            List<Order> result = orderAdapter.findActiveByType(OrderType.SELL);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("deve passar os status PENDING e PARTIALLY_FILLED para o repositório")
        void shouldPassCorrectActiveStatuses() {
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<String>> statusCaptor =
                    ArgumentCaptor.forClass(List.class);

            when(mongoRepository.findByTypeAndStatusIn(anyString(), statusCaptor.capture()))
                    .thenReturn(List.of());

            orderAdapter.findActiveByType(OrderType.BUY);

            assertThat(statusCaptor.getValue())
                    .containsExactlyInAnyOrder("PENDING", "PARTIALLY_FILLED");
        }
    }

    
    @Nested
    @DisplayName("findActiveByUserId()")
    class FindActiveByUserId {

        @Test
        @DisplayName("deve retornar ordens ativas do usuário ordenadas por data desc")
        void shouldReturnActiveOrdersForUser() {
            Order order1 = mock(Order.class);
            Order order2 = mock(Order.class);

            when(mongoRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(
                    eq("user-456"), anyList()))
                    .thenReturn(List.of(orderDocument, orderDocument));
            when(mapper.toDomain(orderDocument))
                    .thenReturn(order1)
                    .thenReturn(order2);

            List<Order> result = orderAdapter.findActiveByUserId(userId);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando usuário não tem ordens ativas")
        void shouldReturnEmptyWhenUserHasNoActiveOrders() {
            when(mongoRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(
                    anyString(), anyList()))
                    .thenReturn(List.of());

            List<Order> result = orderAdapter.findActiveByUserId(userId);

            assertThat(result).isEmpty();
        }
    }

    
    @Nested
    @DisplayName("findByUserIdOrderByCreatedAtDesc()")
    class FindByUserIdOrderByCreatedAtDesc {

        @Test
        @DisplayName("deve executar query paginada com sort DESC por createdAt")
        void shouldExecutePaginatedQueryWithDescSort() {
            Order order1 = mock(Order.class);
            when(mongoTemplate.find(any(Query.class), eq(OrderDocument.class)))
                    .thenReturn(List.of(orderDocument));
            when(mapper.toDomain(orderDocument)).thenReturn(order1);

            List<Order> result =
                    orderAdapter.findByUserIdOrderByCreatedAtDesc(userId, 0, 10);

            assertThat(result).hasSize(1).containsExactly(order1);
            verify(mongoTemplate).find(any(Query.class), eq(OrderDocument.class));
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há resultados")
        void shouldReturnEmptyListWhenNoResults() {
            when(mongoTemplate.find(any(Query.class), eq(OrderDocument.class)))
                    .thenReturn(List.of());

            List<Order> result =
                    orderAdapter.findByUserIdOrderByCreatedAtDesc(userId, 0, 10);

            assertThat(result).isEmpty();
        }


        
        @Nested
        @DisplayName("existsByCorrelationId()")
        class ExistsByCorrelationId {

            @Test
            @DisplayName("deve retornar true quando correlationId existe")
            void shouldReturnTrueWhenExists() {
                when(mongoRepository.existsByCorrelationId("corr-001")).thenReturn(true);

                assertThat(orderAdapter.existsByCorrelationId("corr-001")).isTrue();
            }

            @Test
            @DisplayName("deve retornar false quando correlationId não existe")
            void shouldReturnFalseWhenNotExists() {
                when(mongoRepository.existsByCorrelationId("corr-999")).thenReturn(false);

                assertThat(orderAdapter.existsByCorrelationId("corr-999")).isFalse();
            }
        }

        
        @Nested
        @DisplayName("countByUserId()")
        class CountByUserId {

            @Test
            @DisplayName("deve retornar a contagem correta para o usuário")
            void shouldReturnCorrectCount() {
                when(mongoTemplate.count(any(Query.class), eq(OrderDocument.class)))
                        .thenReturn(7L);

                long count = orderAdapter.countByUserId(userId);

                assertThat(count).isEqualTo(7L);
                verify(mongoTemplate).count(any(Query.class), eq(OrderDocument.class));
            }

            @Test
            @DisplayName("deve retornar zero quando usuário não tem ordens")
            void shouldReturnZeroWhenNoOrders() {
                when(mongoTemplate.count(any(Query.class), eq(OrderDocument.class)))
                        .thenReturn(0L);

                long count = orderAdapter.countByUserId(userId);

                assertThat(count).isZero();
            }
        }
    }
}