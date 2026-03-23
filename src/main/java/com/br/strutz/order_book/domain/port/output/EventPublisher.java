package com.br.strutz.order_book.domain.port.output;

import com.br.strutz.order_book.domain.event.Event;

public interface EventPublisher {

    void publish(Event event);
    void publishAndWait(Event event);
}
