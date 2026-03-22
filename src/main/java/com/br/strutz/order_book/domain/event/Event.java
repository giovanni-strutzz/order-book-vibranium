package com.br.strutz.order_book.domain.event;

import java.time.Instant;

public abstract class Event {

    private final String correlationId;
    private final Instant occurredAt;
    private final String userId;

    protected Event(String correlationId, String userId) {
        this.correlationId = correlationId;
        this.occurredAt = Instant.now();
        this.userId = userId;
    }

    public String  getCorrelationId() { return correlationId; }
    public Instant getOccurredAt()    { return occurredAt;    }
    public String  getUserId()        { return userId;        }
}
