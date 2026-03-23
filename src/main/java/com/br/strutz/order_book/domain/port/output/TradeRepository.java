package com.br.strutz.order_book.domain.port.output;

import com.br.strutz.order_book.domain.model.TradeId;
import com.br.strutz.order_book.domain.model.aggregates.Trade;
import com.br.strutz.order_book.domain.model.user.UserId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TradeRepository {

    Trade save(Trade trade);
    Optional<Trade> findById(TradeId id);
    List<Trade> findByUserIdOrderByExecutedAtDesc(UserId userId, int page, int size);
    List<Trade> findByExecutedAtBetween(Instant from, Instant to);
}
