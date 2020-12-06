package com.vb.market.events;

import com.vb.market.engine.booking.Trade;
import org.springframework.context.ApplicationEvent;

public class TradeTransactionEvent extends ApplicationEvent {

    private final Trade trade;

    public TradeTransactionEvent(Object source, Trade trade) {
        super(source);
        this.trade = trade;
    }

    public Trade getTrade() {
        return trade;
    }
}
