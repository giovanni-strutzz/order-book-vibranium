package com.br.strutz.order_book.domain.model.aggregates;

import com.br.strutz.order_book.domain.exception.InvalidOrderException;
import com.br.strutz.order_book.domain.exception.InvalidOrderStateException;
import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.order.OrderId;
import com.br.strutz.order_book.domain.model.order.OrderStatus;
import com.br.strutz.order_book.domain.model.order.OrderType;
import com.br.strutz.order_book.domain.model.user.UserId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.Objects;

@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class Order {

    private final OrderId id;
    private final UserId userId;
    private final OrderType type;
    private final Money price;
    private final Money quantity;
    private final Instant createdAt;
    private final String correlationId;

    private Money filledQuantity;
    private OrderStatus status;
    private Instant updatedAt;

    private Order(OrderId id, UserId userId, OrderType type,
                  Money price, Money quantity, String correlationId) {
        this.id             = Objects.requireNonNull(id);
        this.userId         = Objects.requireNonNull(userId);
        this.type           = Objects.requireNonNull(type);
        this.price          = Objects.requireNonNull(price);
        this.quantity       = Objects.requireNonNull(quantity);
        this.correlationId  = Objects.requireNonNull(correlationId);
        this.filledQuantity = Money.zero();
        this.status         = OrderStatus.PENDING;
        this.createdAt      = Instant.now();
        this.updatedAt      = this.createdAt;
    };

    public static Order create(UserId userId, OrderType type,
                               Money price, Money quantity,
                               String correlationId) {
        if (price.isZero()) {
            throw new InvalidOrderException("Price cannot be zero");
        }

        if (quantity.isZero()) {
            throw new InvalidOrderException("Quantity cannot be zero");
        }

        return new Order(OrderId.generate(), userId, type, price, quantity, correlationId);
    }

    public static Order reconstitute(OrderId id, UserId userId, OrderType type,
                                     Money price, Money quantity,
                                     Money filledQuantity, OrderStatus status,
                                     Instant createdAt, Instant updatedAt,
                                     String correlationId) {
        var order = new Order(id, userId, type, price, quantity, correlationId);
        order.filledQuantity = filledQuantity;
        order.status         = status;
        order.updatedAt      = updatedAt;
        return order;
    };

    public void fill(Money tradedQuantity) {
        assertActive();
        this.filledQuantity = this.filledQuantity.add(tradedQuantity);
        this.status = this.filledQuantity.getAmount()
                .compareTo(this.quantity.getAmount()) >= 0
                ? OrderStatus.FILLED
                : OrderStatus.PARTIALLY_FILLED;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        assertActive();
        this.status    = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public Money remainingQuantity() {
        return Money.of(
                quantity.getAmount().subtract(filledQuantity.getAmount())
        );
    }

    public boolean isFilled()  {
        return status == OrderStatus.FILLED;
    }

    public boolean isActive()  {
        return status.isActive();
    }

    public boolean matchesPriceOf(Order counterpart) {
        if (this.type == OrderType.BUY) {
            return this.price.getAmount().compareTo(counterpart.price.getAmount()) >= 0;
        }
        else {
            return this.price.getAmount().compareTo(counterpart.price.getAmount()) <= 0;
        }
    }

    private void assertActive() {
        if (status.isTerminal())
            throw new InvalidOrderStateException(
                    "Order %s already in TERMINAL state: %s".formatted(id, status));
    }
}
