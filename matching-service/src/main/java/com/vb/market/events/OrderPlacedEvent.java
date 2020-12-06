package com.vb.market.events;

import com.vb.market.engine.TradeManagingActor.OrderPlacedReply;
import org.springframework.context.ApplicationEvent;

public class OrderPlacedEvent extends ApplicationEvent {

    private final OrderPlacedReply orderPlacedReply;

    public OrderPlacedEvent(Object source, OrderPlacedReply orderPlacedReply) {
        super(source);
        this.orderPlacedReply = orderPlacedReply;
    }

    public OrderPlacedReply getOrderPlacedReply() {
        return orderPlacedReply;
    }
}
