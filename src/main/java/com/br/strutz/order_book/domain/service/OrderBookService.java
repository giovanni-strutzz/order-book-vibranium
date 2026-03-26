package com.br.strutz.order_book.domain.service;

import com.br.strutz.order_book.application.query.OrderBookSnapshot;
import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.aggregates.Order;
import com.br.strutz.order_book.domain.model.aggregates.Trade;
import com.br.strutz.order_book.domain.model.order.OrderType;
import com.br.strutz.order_book.application.query.OrderBookSnapshot.PriceLevel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class OrderBookService {

    private final TreeMap<BigDecimal, LinkedList<Order>> bids = new TreeMap<>(Comparator.reverseOrder());
    private final TreeMap<BigDecimal, LinkedList<Order>> asks = new TreeMap<>();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();


    public List<Trade> match(Order incoming) {
        writeLock.lock();

        try {
            return incoming.getType() == OrderType.BUY ? matchBuy(incoming) : matchSell(incoming);
        } finally {
            writeLock.unlock();
        }
    }

    private List<Trade> matchBuy(Order buyOrder) {
        List<Trade> trades = new ArrayList<>();

        while (!asks.isEmpty() && buyOrder.isActive()) {
            Map.Entry<BigDecimal, LinkedList<Order>> bestAsk = asks.firstEntry();

            if (buyOrder.getPrice().getAmount().compareTo(bestAsk.getKey()) < 0) break;

            trades.addAll(executeMatches(buyOrder, bestAsk.getValue(), bestAsk.getKey()));

            if (bestAsk.getValue().isEmpty()) asks.remove(bestAsk.getKey());
        }

        if (buyOrder.isActive()) addToBook(bids, buyOrder);

        return trades;
    }

    private List<Trade> matchSell(Order sellOrder) {
        List<Trade> trades = new ArrayList<>();

        while (!bids.isEmpty() && sellOrder.isActive()) {
            Map.Entry<BigDecimal, LinkedList<Order>> bestBid = bids.firstEntry();

            if (sellOrder.getPrice().getAmount().compareTo(bestBid.getKey()) > 0) break;

            trades.addAll(executeMatches(sellOrder, bestBid.getValue(), bestBid.getKey()));

            if (bestBid.getValue().isEmpty()) bids.remove(bestBid.getKey());
        }

        if (sellOrder.isActive()) addToBook(asks, sellOrder);

        return trades;
    };

    private List<Trade> executeMatches(Order aggressor,
                                       LinkedList<Order> restingQueue,
                                       BigDecimal executionPrice) {
        List<Trade> trades = new ArrayList<>();

        while (!restingQueue.isEmpty() && aggressor.isActive()) {
            Order resting = restingQueue.peek();

            assert resting != null;

            Money tradedQty = minQuantity(aggressor.remainingQuantity(),
                    resting.remainingQuantity());

            Money executionMoney = Money.of(executionPrice);

            Trade trade = Trade.execute(
                    aggressor.getType() == OrderType.BUY ? aggressor : resting,
                    aggressor.getType() == OrderType.SELL ? aggressor : resting,
                    tradedQty
            );
            trades.add(trade);

            aggressor.fill(tradedQty);
            resting.fill(tradedQty);

            if (resting.isFilled()) restingQueue.poll();
        }

        return trades;
    };

    public boolean remove(Order order) {
        writeLock.lock();
        try {
            TreeMap<BigDecimal, LinkedList<Order>> book =
                    order.getType() == OrderType.BUY ? bids : asks;

            LinkedList<Order> queue = book.get(order.getPrice().getAmount());
            if (queue == null) return false;

            boolean removed = queue.removeIf(
                    o -> o.getId().equals(order.getId()));

            if (queue.isEmpty()) book.remove(order.getPrice().getAmount());

            return removed;
        } finally {
            writeLock.unlock();
        }
    };

    public OrderBookSnapshot toSnapshot(int depth) {
        readLock.lock();
        try {
            List<PriceLevel> bidLevels = buildLevels(bids, depth);
            List<PriceLevel> askLevels = buildLevels(asks, depth);

            BigDecimal spread = calculateSpread(bidLevels, askLevels);

            return OrderBookSnapshot.builder()
                    .bids(bidLevels)
                    .asks(askLevels)
                    .spread(spread)
                    .generatedAt(java.time.Instant.now())
                    .build();
        } finally {
            readLock.unlock();
        }
    }

    private List<PriceLevel> buildLevels(TreeMap<BigDecimal, LinkedList<Order>> book,
                                         int depth) {
        List<PriceLevel> levels = new ArrayList<>();

        for (Map.Entry<BigDecimal, LinkedList<Order>> entry : book.entrySet()) {
            if (levels.size() >= depth) break;

            BigDecimal totalQty = entry.getValue().stream()
                    .map(o -> o.remainingQuantity().getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            levels.add(PriceLevel.builder()
                    .price(entry.getKey())
                    .quantity(totalQty)
                    .orderCount(entry.getValue().size())
                    .build());
        }

        return Collections.unmodifiableList(levels);
    }

    private BigDecimal calculateSpread(List<OrderBookSnapshot.PriceLevel> bids, List<OrderBookSnapshot.PriceLevel> asks) {
        if (bids.isEmpty() || asks.isEmpty()) return BigDecimal.ZERO;
        return asks.getFirst().getPrice().subtract(bids.getFirst().getPrice());
    };

    public void initialize(List<Order> pendingBuys, List<Order> pendingSells) {
        writeLock.lock();
        try {
            bids.clear();
            asks.clear();
            pendingBuys.forEach(o -> addToBook(bids, o));
            pendingSells.forEach(o -> addToBook(asks, o));
        } finally {
            writeLock.unlock();
        }
    }

    private void addToBook(TreeMap<BigDecimal, LinkedList<Order>> book, Order order) {
        book.computeIfAbsent(order.getPrice().getAmount(), k -> new LinkedList<>())
                .addLast(order);
    }

    private Money minQuantity(Money a, Money b) {
        return a.getAmount().compareTo(b.getAmount()) <= 0 ? a : b;
    }

    public int getBidDepth() {
        readLock.lock();
        try { return bids.size(); } finally { readLock.unlock(); }
    }

    public int getAskDepth() {
        readLock.lock();
        try { return asks.size(); } finally { readLock.unlock(); }
    }

    public boolean isEmpty() {
        readLock.lock();
        try { return bids.isEmpty() && asks.isEmpty(); } finally { readLock.unlock(); }
    }
}
