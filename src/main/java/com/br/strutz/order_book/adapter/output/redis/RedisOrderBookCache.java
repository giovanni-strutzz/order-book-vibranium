package com.br.strutz.order_book.adapter.output.redis;

import com.br.strutz.order_book.application.query.OrderBookSnapshot;
import com.br.strutz.order_book.domain.port.output.order.OrderBookCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class RedisOrderBookCache implements OrderBookCache {

    private static final Logger log =
            LoggerFactory.getLogger(RedisOrderBookCache.class);

    private static final String CACHE_KEY = "orderbook:vibranium:snapshot";

    private final RedisTemplate<String, OrderBookSnapshot> redisTemplate;
    private final Duration ttl;

    public RedisOrderBookCache(
            RedisTemplate<String, OrderBookSnapshot> redisTemplate,
            @Value("${vibranium.cache.order-book-ttl:5s}") Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.ttl           = ttl;
    }

    @Override
    public void saveSnapshot(OrderBookSnapshot snapshot) {
        try {
            redisTemplate.opsForValue().set(CACHE_KEY, snapshot, ttl);
            log.debug("Order book snapshot cached — ttl={}s bids={} asks={}",
                    ttl.getSeconds(),
                    snapshot.getBids().size(),
                    snapshot.getAsks().size());
        } catch (Exception e) {
            log.error("Failed to cache order book snapshot — {}",
                    e.getMessage());
        }
    }

    @Override
    public Optional<OrderBookSnapshot> getSnapshot() {
        try {
            OrderBookSnapshot snapshot = redisTemplate.opsForValue().get(CACHE_KEY);
            if (snapshot != null) {
                log.debug("Order book snapshot cache hit");
            }
            return Optional.ofNullable(snapshot);
        } catch (Exception e) {
            log.error("Failed to read order book snapshot from cache — {}",
                    e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void invalidate() {
        try {
            redisTemplate.delete(CACHE_KEY);
            log.debug("Order book snapshot cache invalidated");
        } catch (Exception e) {
            log.error("Failed to invalidate order book snapshot cache — {}",
                    e.getMessage());
        }
    }

    @Override
    public long getTtlSeconds() {
        try {
            Long ttlRemaining = redisTemplate.getExpire(CACHE_KEY, TimeUnit.SECONDS);
            return ttlRemaining != null ? ttlRemaining : 0L;
        } catch (Exception e) {
            log.error("Failed to get TTL from cache — {}", e.getMessage());
            return 0L;
        }
    }
}
