package com.vb.market.engine.booking;

public class MatchResult {

    private OrderBook.KeyPriority buySide;

    private OrderBook.KeyPriority sellSide;

    public MatchResult(OrderBook.KeyPriority buySide, OrderBook.KeyPriority sellSide) {
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
