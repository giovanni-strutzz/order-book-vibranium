package com.br.strutz.order_book.domain.service;

import com.br.strutz.order_book.domain.model.aggregates.Order;
import com.br.strutz.order_book.domain.model.order.OrderType;
import com.br.strutz.order_book.domain.port.output.order.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderBookInitializerService")
class OrderBookInitializerServiceTest {

    @Mock private OrderBookService orderBook;
    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private OrderBookInitializerService initializerService;

    // ── initialize() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("initialize()")
    class Initialize {

        @Test
        @DisplayName("deve buscar ordens BUY e SELL ativas e delegar ao orderBook")
        void shouldFetchActiveOrdersAndDelegateToOrderBook() {
            Order buy  = mock(Order.class);
            Order sell = mock(Order.class);

            when(orderRepository.findActiveByType(OrderType.BUY)).thenReturn(List.of(buy));
            when(orderRepository.findActiveByType(OrderType.SELL)).thenReturn(List.of(sell));
            when(orderBook.getBidDepth()).thenReturn(1);
            when(orderBook.getAskDepth()).thenReturn(1);

            initializerService.initialize();

            verify(orderRepository).findActiveByType(OrderType.BUY);
            verify(orderRepository).findActiveByType(OrderType.SELL);
            verify(orderBook).initialize(List.of(buy), List.of(sell));
        }

        @Test
        @DisplayName("deve inicializar com listas vazias quando não há ordens ativas")
        void shouldInitializeWithEmptyListsWhenNoActiveOrders() {
            when(orderRepository.findActiveByType(OrderType.BUY)).thenReturn(List.of());
            when(orderRepository.findActiveByType(OrderType.SELL)).thenReturn(List.of());
            when(orderBook.getBidDepth()).thenReturn(0);
            when(orderBook.getAskDepth()).thenReturn(0);

            initializerService.initialize();

            verify(orderBook).initialize(List.of(), List.of());
        }

        @Test
        @DisplayName("deve inicializar com múltiplas ordens em cada lado")
        void shouldInitializeWithMultipleOrdersOnEachSide() {
            List<Order> buys  = List.of(mock(Order.class), mock(Order.class));
            List<Order> sells = List.of(mock(Order.class), mock(Order.class), mock(Order.class));

            when(orderRepository.findActiveByType(OrderType.BUY)).thenReturn(buys);
            when(orderRepository.findActiveByType(OrderType.SELL)).thenReturn(sells);
            when(orderBook.getBidDepth()).thenReturn(2);
            when(orderBook.getAskDepth()).thenReturn(3);

            initializerService.initialize();

            verify(orderBook).initialize(buys, sells);
        }

        @Test
        @DisplayName("deve consultar profundidade do livro após inicializar para logging")
        void shouldQueryDepthAfterInitializeForLogging() {
            when(orderRepository.findActiveByType(any())).thenReturn(List.of());
            when(orderBook.getBidDepth()).thenReturn(0);
            when(orderBook.getAskDepth()).thenReturn(0);

            initializerService.initialize();

            // getBidDepth e getAskDepth são chamados para compor o log de conclusão
            verify(orderBook).getBidDepth();
            verify(orderBook).getAskDepth();
        }

        @Test
        @DisplayName("deve chamar initialize() exatamente uma vez no orderBook")
        void shouldCallInitializeExactlyOnce() {
            when(orderRepository.findActiveByType(any())).thenReturn(List.of());
            when(orderBook.getBidDepth()).thenReturn(0);
            when(orderBook.getAskDepth()).thenReturn(0);

            initializerService.initialize();

            verify(orderBook, times(1)).initialize(any(), any());
        }

        @Test
        @DisplayName("deve propagar exceção lançada pelo repositório")
        void shouldPropagateRepositoryException() {
            when(orderRepository.findActiveByType(OrderType.BUY))
                    .thenThrow(new RuntimeException("MongoDB unavailable"));

            org.junit.jupiter.api.Assertions.assertThrows(
                    RuntimeException.class,
                    () -> initializerService.initialize()
            );

            verify(orderBook, never()).initialize(any(), any());
        }

        @Test
        @DisplayName("deve propagar exceção lançada pelo orderBook durante inicialização")
        void shouldPropagateOrderBookException() {
            when(orderRepository.findActiveByType(any())).thenReturn(List.of());
            doThrow(new RuntimeException("OrderBook init failed"))
                    .when(orderBook).initialize(any(), any());

            org.junit.jupiter.api.Assertions.assertThrows(
                    RuntimeException.class,
                    () -> initializerService.initialize()
            );

            verify(orderBook, never()).getBidDepth();
            verify(orderBook, never()).getAskDepth();
        }

        @Test
        @DisplayName("deve passar as listas exatas retornadas pelo repositório sem modificações")
        void shouldPassExactListsReturnedByRepository() {
            List<Order> buys  = List.of(mock(Order.class));
            List<Order> sells = List.of(mock(Order.class));

            when(orderRepository.findActiveByType(OrderType.BUY)).thenReturn(buys);
            when(orderRepository.findActiveByType(OrderType.SELL)).thenReturn(sells);
            when(orderBook.getBidDepth()).thenReturn(1);
            when(orderBook.getAskDepth()).thenReturn(1);

            initializerService.initialize();

            // Garante que as referências exatas chegam ao orderBook, sem cópia ou transformação
            verify(orderBook).initialize(same(buys), same(sells));
        }
    }
}