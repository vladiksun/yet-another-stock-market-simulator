package com.vb.market.events;

import org.springframework.context.ApplicationEvent;

public class BookBalanceEvent extends ApplicationEvent {

    public BookBalanceEvent(Object source) {
        super(source);
    }
}
