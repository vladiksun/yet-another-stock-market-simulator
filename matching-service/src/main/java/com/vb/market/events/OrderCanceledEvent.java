package com.vb.market.events;

import com.vb.market.engine.TradeManagingActor.CancelOrderReply;
import org.springframework.context.ApplicationEvent;

public class OrderCanceledEvent extends ApplicationEvent {

    private final CancelOrderReply cancelOrderReply;

    public CancelOrderReply getCancelOrderReply() {
        return cancelOrderReply;
    }

    public OrderCanceledEvent(Object source, CancelOrderReply cancelOrderReply) {
        super(source);
        this.cancelOrderReply = cancelOrderReply;
    }
}
