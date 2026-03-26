package com.br.strutz.order_book.domain.service;

import com.br.strutz.order_book.application.query.OrderBookSnapshot;
import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.aggregates.Order;
import com.br.strutz.order_book.domain.model.aggregates.Trade;
import com.br.strutz.order_book.domain.model.order.OrderId;
import com.br.strutz.order_book.domain.model.order.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@DisplayName("OrderBookService")
class OrderBookServiceTest {

    

    
    private static Order orderMock(String id, OrderType type,
                                   BigDecimal price, BigDecimal qty) {
        Order order = mock(Order.class);
        OrderId orderId = mock(OrderId.class);

        when(orderId.getValue()).thenReturn(id);
        when(order.getId()).thenReturn(orderId);
        when(order.getType()).thenReturn(type);

        Money priceMoney = mock(Money.class);
        when(priceMoney.getAmount()).thenReturn(price);
        when(order.getPrice()).thenReturn(priceMoney);

        
        Money qtyMoney = mock(Money.class);
        when(qtyMoney.getAmount()).thenReturn(qty);
        when(order.remainingQuantity()).thenReturn(qtyMoney);

        when(order.isActive()).thenReturn(true);
        when(order.isFilled()).thenReturn(false);

        return order;
    }

    
    private static Order fullyFillableOrder(String id, OrderType type,
                                            BigDecimal price, BigDecimal qty) {
        Order order = mock(Order.class);
        OrderId orderId = mock(OrderId.class);

        when(orderId.getValue()).thenReturn(id);
        when(order.getId()).thenReturn(orderId);
        when(order.getType()).thenReturn(type);

        Money priceMoney = mock(Money.class);
        when(priceMoney.getAmount()).thenReturn(price);
        when(order.getPrice()).thenReturn(priceMoney);

        Money qtyMoney = mock(Money.class);
        when(qtyMoney.getAmount()).thenReturn(qty);
        when(order.remainingQuantity()).thenReturn(qtyMoney);

        
        doAnswer(inv -> {
            when(order.isActive()).thenReturn(false);
            when(order.isFilled()).thenReturn(true);
            return null;
        }).when(order).fill(any());

        when(order.isActive()).thenReturn(true);
        when(order.isFilled()).thenReturn(false);

        return order;
    }

    

    @Nested
    @DisplayName("isEmpty() / getBidDepth() / getAskDepth()")
    class StateQueries {

        private OrderBookService service;

        @BeforeEach
        void setUp() {
            service = new OrderBookService();
        }

        @Test
        @DisplayName("livro recém-criado deve estar vazio")
        void newBookIsEmpty() {
            assertThat(service.isEmpty()).isTrue();
            assertThat(service.getBidDepth()).isZero();
            assertThat(service.getAskDepth()).isZero();
        }

        @Test
        @DisplayName("deve refletir profundidade correta após inicialização")
        void depthReflectsInitializedOrders() {
            Order buy1  = orderMock("b1", OrderType.BUY,  new BigDecimal("100"), BigDecimal.ONE);
            Order buy2  = orderMock("b2", OrderType.BUY,  new BigDecimal("99"),  BigDecimal.ONE);
            Order sell1 = orderMock("s1", OrderType.SELL, new BigDecimal("101"), BigDecimal.ONE);

            service.initialize(List.of(buy1, buy2), List.of(sell1));

            assertThat(service.isEmpty()).isFalse();
            assertThat(service.getBidDepth()).isEqualTo(2);
            assertThat(service.getAskDepth()).isEqualTo(1);
        }

        @Test
        @DisplayName("duas ordens de compra no mesmo preço devem resultar em profundidade 1")
        void samePriceBidsCountAsOneLevel() {
            Order buy1 = orderMock("b1", OrderType.BUY, new BigDecimal("100"), BigDecimal.ONE);
            Order buy2 = orderMock("b2", OrderType.BUY, new BigDecimal("100"), BigDecimal.ONE);

            service.initialize(List.of(buy1, buy2), List.of());

            assertThat(service.getBidDepth()).isEqualTo(1);
        }
    }

    

    @Nested
    @DisplayName("initialize()")
    class Initialize {

        private OrderBookService service;

        @BeforeEach
        void setUp() {
            service = new OrderBookService();
        }

        @Test
        @DisplayName("deve limpar estado anterior e recarregar com novas ordens")
        void shouldClearAndReload() {
            Order oldBuy = orderMock("old", OrderType.BUY, new BigDecimal("50"), BigDecimal.ONE);
            service.initialize(List.of(oldBuy), List.of());
            assertThat(service.getBidDepth()).isEqualTo(1);

            Order newBuy1 = orderMock("n1", OrderType.BUY, new BigDecimal("110"), BigDecimal.ONE);
            Order newBuy2 = orderMock("n2", OrderType.BUY, new BigDecimal("109"), BigDecimal.ONE);
            Order newSell = orderMock("n3", OrderType.SELL, new BigDecimal("111"), BigDecimal.ONE);

            service.initialize(List.of(newBuy1, newBuy2), List.of(newSell));

            assertThat(service.getBidDepth()).isEqualTo(2);
            assertThat(service.getAskDepth()).isEqualTo(1);
        }

        @Test
        @DisplayName("deve aceitar listas vazias sem erros")
        void shouldAcceptEmptyLists() {
            service.initialize(List.of(), List.of());
            assertThat(service.isEmpty()).isTrue();
        }
    }

    

    @Nested
    @DisplayName("match() — BUY")
    class MatchBuy {

        private OrderBookService service;

        @BeforeEach
        void setUp() {
            service = new OrderBookService();
        }

        @Test
        @DisplayName("ordem de compra sem asks disponíveis deve entrar no livro sem gerar trades")
        void buyWithNoAsksGoesToBook() {
            Order buy = orderMock("b1", OrderType.BUY, new BigDecimal("100"), BigDecimal.ONE);

            List<Trade> trades = service.match(buy);

            assertThat(trades).isEmpty();
            assertThat(service.getBidDepth()).isEqualTo(1);
        }

        @Test
        @DisplayName("ordem de compra abaixo do melhor ask deve entrar no livro sem gerar trades")
        void buyBelowBestAskGoesToBook() {
            Order sell = orderMock("s1", OrderType.SELL, new BigDecimal("105"), BigDecimal.ONE);
            service.initialize(List.of(), List.of(sell));

            Order buy = orderMock("b1", OrderType.BUY, new BigDecimal("100"), BigDecimal.ONE);
            List<Trade> trades = service.match(buy);

            assertThat(trades).isEmpty();
            assertThat(service.getBidDepth()).isEqualTo(1);
            assertThat(service.getAskDepth()).isEqualTo(1);
        }

        @Test
        @DisplayName("ordem de compra que cruza o ask deve gerar um trade e remover o ask")
        void buyCrossesAskGeneratesTrade() {
            Order sell = fullyFillableOrder("s1", OrderType.SELL,
                    new BigDecimal("100"), BigDecimal.ONE);
            service.initialize(List.of(), List.of(sell));

            Order buy = fullyFillableOrder("b1", OrderType.BUY,
                    new BigDecimal("100"), BigDecimal.ONE);
            List<Trade> trades = service.match(buy);

            assertThat(trades).hasSize(1);
            assertThat(service.getAskDepth()).isZero();
            assertThat(service.getBidDepth()).isZero();
        }

        @Test
        @DisplayName("ordem de compra acima do ask deve cruzar (preço favorável)")
        void buyAboveBestAskAlsoCrosses() {
            Order sell = fullyFillableOrder("s1", OrderType.SELL,
                    new BigDecimal("99"), BigDecimal.ONE);
            service.initialize(List.of(), List.of(sell));

            Order buy = fullyFillableOrder("b1", OrderType.BUY,
                    new BigDecimal("105"), BigDecimal.ONE);
            List<Trade> trades = service.match(buy);

            assertThat(trades).hasSize(1);
        }

        @Test
        @DisplayName("buy não totalmente preenchido deve permanecer no livro após match parcial")
        void partiallyFilledBuyRemainsInBook() {
            
            Order sell = fullyFillableOrder("s1", OrderType.SELL,
                    new BigDecimal("100"), BigDecimal.ONE);
            service.initialize(List.of(), List.of(sell));

            
            Order buy = orderMock("b1", OrderType.BUY, new BigDecimal("100"),
                    new BigDecimal("2"));
            

            List<Trade> trades = service.match(buy);

            assertThat(trades).hasSize(1);
            assertThat(service.getAskDepth()).isZero();   
            assertThat(service.getBidDepth()).isEqualTo(1); 
        }

        @Test
        @DisplayName("deve cruzar múltiplos níveis de ask em sequência")
        void buyCrossesMultipleAskLevels() {
            Order sell1 = fullyFillableOrder("s1", OrderType.SELL,
                    new BigDecimal("99"), BigDecimal.ONE);
            Order sell2 = fullyFillableOrder("s2", OrderType.SELL,
                    new BigDecimal("100"), BigDecimal.ONE);
            service.initialize(List.of(), List.of(sell1, sell2));

            Order buy = mock(Order.class);
            OrderId buyId = mock(OrderId.class);
            when(buyId.getValue()).thenReturn("b1");
            when(buy.getId()).thenReturn(buyId);
            when(buy.getType()).thenReturn(OrderType.BUY);
            Money bp = mock(Money.class);
            when(bp.getAmount()).thenReturn(new BigDecimal("105"));
            when(buy.getPrice()).thenReturn(bp);
            Money bq = mock(Money.class);
            when(bq.getAmount()).thenReturn(BigDecimal.ONE);
            when(buy.remainingQuantity()).thenReturn(bq);

            
            when(buy.isActive()).thenReturn(true, false);
            when(buy.isFilled()).thenReturn(false, true);

            List<Trade> trades = service.match(buy);

            assertThat(trades).hasSize(0);
        }
    }

    

    @Nested
    @DisplayName("match() — SELL")
    class MatchSell {

        private OrderBookService service;

        @BeforeEach
        void setUp() {
            service = new OrderBookService();
        }

        @Test
        @DisplayName("ordem de venda sem bids deve entrar no livro sem gerar trades")
        void sellWithNoBidsGoesToBook() {
            Order sell = orderMock("s1", OrderType.SELL, new BigDecimal("100"), BigDecimal.ONE);

            List<Trade> trades = service.match(sell);

            assertThat(trades).isEmpty();
            assertThat(service.getAskDepth()).isEqualTo(1);
        }

        @Test
        @DisplayName("ordem de venda acima do melhor bid deve entrar no livro sem gerar trades")
        void sellAboveBestBidGoesToBook() {
            Order buy = orderMock("b1", OrderType.BUY, new BigDecimal("95"), BigDecimal.ONE);
            service.initialize(List.of(buy), List.of());

            Order sell = orderMock("s1", OrderType.SELL, new BigDecimal("100"), BigDecimal.ONE);
            List<Trade> trades = service.match(sell);

            assertThat(trades).isEmpty();
            assertThat(service.getAskDepth()).isEqualTo(1);
            assertThat(service.getBidDepth()).isEqualTo(1);
        }

        @Test
        @DisplayName("ordem de venda que cruza o bid deve gerar um trade e remover o bid")
        void sellCrossesBidGeneratesTrade() {
            Order buy = fullyFillableOrder("b1", OrderType.BUY,
                    new BigDecimal("100"), BigDecimal.ONE);
            service.initialize(List.of(buy), List.of());

            Order sell = fullyFillableOrder("s1", OrderType.SELL,
                    new BigDecimal("100"), BigDecimal.ONE);
            List<Trade> trades = service.match(sell);

            assertThat(trades).hasSize(1);
            assertThat(service.getBidDepth()).isZero();
            assertThat(service.getAskDepth()).isZero();
        }

        @Test
        @DisplayName("ordem de venda abaixo do bid deve cruzar (preço favorável)")
        void sellBelowBestBidAlsoCrosses() {
            Order buy = fullyFillableOrder("b1", OrderType.BUY,
                    new BigDecimal("105"), BigDecimal.ONE);
            service.initialize(List.of(buy), List.of());

            Order sell = fullyFillableOrder("s1", OrderType.SELL,
                    new BigDecimal("100"), BigDecimal.ONE);
            List<Trade> trades = service.match(sell);

            assertThat(trades).hasSize(1);
        }

        @Test
        @DisplayName("sell não totalmente preenchido deve permanecer no livro após match parcial")
        void partiallyFilledSellRemainsInBook() {
            Order buy = fullyFillableOrder("b1", OrderType.BUY,
                    new BigDecimal("100"), BigDecimal.ONE);
            service.initialize(List.of(buy), List.of());

            
            Order sell = orderMock("s1", OrderType.SELL, new BigDecimal("100"),
                    new BigDecimal("2"));

            List<Trade> trades = service.match(sell);

            assertThat(trades).hasSize(1);
            assertThat(service.getBidDepth()).isZero();    
            assertThat(service.getAskDepth()).isEqualTo(1); 
        }
    }

    

    @Nested
    @DisplayName("remove()")
    class Remove {

        private OrderBookService service;

        @BeforeEach
        void setUp() {
            service = new OrderBookService();
        }

        @Test
        @DisplayName("deve remover ordem de compra existente e retornar true")
        void shouldRemoveExistingBidAndReturnTrue() {
            Order buy = orderMock("b1", OrderType.BUY, new BigDecimal("100"), BigDecimal.ONE);
            service.initialize(List.of(buy), List.of());

            boolean removed = service.remove(buy);

            assertThat(removed).isTrue();
            assertThat(service.getBidDepth()).isZero();
        }

        @Test
        @DisplayName("deve remover ordem de venda existente e retornar true")
        void shouldRemoveExistingAskAndReturnTrue() {
            Order sell = orderMock("s1", OrderType.SELL, new BigDecimal("100"), BigDecimal.ONE);
            service.initialize(List.of(), List.of(sell));

            boolean removed = service.remove(sell);

            assertThat(removed).isTrue();
            assertThat(service.getAskDepth()).isZero();
        }

        @Test
        @DisplayName("deve retornar false para preço que não existe no livro")
        void shouldReturnFalseWhenPriceLevelMissing() {
            Order ghost = orderMock("x1", OrderType.BUY, new BigDecimal("999"), BigDecimal.ONE);

            boolean removed = service.remove(ghost);

            assertThat(removed).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para id inexistente no nível de preço")
        void shouldReturnFalseWhenIdNotFoundAtPriceLevel() {
            Order real  = orderMock("real",  OrderType.BUY, new BigDecimal("100"), BigDecimal.ONE);
            Order ghost = orderMock("ghost", OrderType.BUY, new BigDecimal("100"), BigDecimal.ONE);
            service.initialize(List.of(real), List.of());

            boolean removed = service.remove(ghost);

            assertThat(removed).isFalse();
            assertThat(service.getBidDepth()).isEqualTo(1); 
        }

        @Test
        @DisplayName("deve remover uma de duas ordens no mesmo nível e manter o nível")
        void shouldRemoveOneOfTwoOrdersAtSameLevel() {
            Order buy1 = orderMock("b1", OrderType.BUY, new BigDecimal("100"), BigDecimal.ONE);
            Order buy2 = orderMock("b2", OrderType.BUY, new BigDecimal("100"), BigDecimal.ONE);
            service.initialize(List.of(buy1, buy2), List.of());

            boolean removed = service.remove(buy1);

            assertThat(removed).isTrue();
            assertThat(service.getBidDepth()).isEqualTo(1); 
        }
    }

    

    @Nested
    @DisplayName("toSnapshot()")
    class ToSnapshot {

        private OrderBookService service;

        @BeforeEach
        void setUp() {
            service = new OrderBookService();
        }

        @Test
        @DisplayName("snapshot de livro vazio deve ter spread zero e listas vazias")
        void emptyBookSnapshotHasZeroSpreadAndEmptyLists() {
            OrderBookSnapshot snap = service.toSnapshot(5);

            assertThat(snap.getBids()).isEmpty();
            assertThat(snap.getAsks()).isEmpty();
            assertThat(snap.getSpread()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(snap.getGeneratedAt()).isNotNull();
        }

        @Test
        @DisplayName("snapshot deve agregar quantidade total por nível de preço")
        void snapshotAggregatesQuantityPerPriceLevel() {
            Order buy1 = orderMock("b1", OrderType.BUY, new BigDecimal("100"), new BigDecimal("3"));
            Order buy2 = orderMock("b2", OrderType.BUY, new BigDecimal("100"), new BigDecimal("2"));
            service.initialize(List.of(buy1, buy2), List.of());

            OrderBookSnapshot snap = service.toSnapshot(5);

            assertThat(snap.getBids()).hasSize(1);
            OrderBookSnapshot.PriceLevel level = snap.getBids().getFirst();
            assertThat(level.getPrice()).isEqualByComparingTo("100");
            assertThat(level.getQuantity()).isEqualByComparingTo("5");
            assertThat(level.getOrderCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("snapshot deve respeitar o limite de depth")
        void snapshotRespectsDepthLimit() {
            Order b1 = orderMock("b1", OrderType.BUY, new BigDecimal("103"), BigDecimal.ONE);
            Order b2 = orderMock("b2", OrderType.BUY, new BigDecimal("102"), BigDecimal.ONE);
            Order b3 = orderMock("b3", OrderType.BUY, new BigDecimal("101"), BigDecimal.ONE);
            service.initialize(List.of(b1, b2, b3), List.of());

            OrderBookSnapshot snap = service.toSnapshot(2);

            assertThat(snap.getBids()).hasSize(2);
            
            assertThat(snap.getBids().get(0).getPrice()).isEqualByComparingTo("103");
            assertThat(snap.getBids().get(1).getPrice()).isEqualByComparingTo("102");
        }

        @Test
        @DisplayName("spread deve ser calculado como melhor ask − melhor bid")
        void spreadIsCalculatedCorrectly() {
            Order buy  = orderMock("b1", OrderType.BUY,  new BigDecimal("98"), BigDecimal.ONE);
            Order sell = orderMock("s1", OrderType.SELL, new BigDecimal("102"), BigDecimal.ONE);
            service.initialize(List.of(buy), List.of(sell));

            OrderBookSnapshot snap = service.toSnapshot(5);

            
            assertThat(snap.getSpread()).isEqualByComparingTo("4");
        }

        @Test
        @DisplayName("spread deve ser zero quando apenas bids existem")
        void spreadIsZeroWithOnlyBids() {
            Order buy = orderMock("b1", OrderType.BUY, new BigDecimal("100"), BigDecimal.ONE);
            service.initialize(List.of(buy), List.of());

            OrderBookSnapshot snap = service.toSnapshot(5);

            assertThat(snap.getSpread()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("spread deve ser zero quando apenas asks existem")
        void spreadIsZeroWithOnlyAsks() {
            Order sell = orderMock("s1", OrderType.SELL, new BigDecimal("100"), BigDecimal.ONE);
            service.initialize(List.of(), List.of(sell));

            OrderBookSnapshot snap = service.toSnapshot(5);

            assertThat(snap.getSpread()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("asks devem aparecer em ordem crescente de preço no snapshot")
        void asksAreInAscendingPriceOrder() {
            Order s1 = orderMock("s1", OrderType.SELL, new BigDecimal("105"), BigDecimal.ONE);
            Order s2 = orderMock("s2", OrderType.SELL, new BigDecimal("103"), BigDecimal.ONE);
            Order s3 = orderMock("s3", OrderType.SELL, new BigDecimal("101"), BigDecimal.ONE);
            service.initialize(List.of(), List.of(s1, s2, s3));

            OrderBookSnapshot snap = service.toSnapshot(5);

            assertThat(snap.getAsks()).extracting(OrderBookSnapshot.PriceLevel::getPrice)
                    .containsExactly(
                            new BigDecimal("101"),
                            new BigDecimal("103"),
                            new BigDecimal("105"));
        }
    }
}