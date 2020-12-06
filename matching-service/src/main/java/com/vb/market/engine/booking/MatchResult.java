package com.vb.market.engine.booking;

import com.vb.market.engine.booking.OrderBook.KeyPriority;

public class MatchResult {

    private OrderBook.KeyPriority buySide;

    private OrderBook.KeyPriority sellSide;

    public MatchResult(KeyPriority buySide, KeyPriority sellSide) {
        this.buySide = buySide;
        this.sellSide = sellSide;
    }

    public OrderBook.KeyPriority getBuySide() {
        return buySide;
    }

    public OrderBook.KeyPriority getSellSide() {
        return sellSide;
    }
}
