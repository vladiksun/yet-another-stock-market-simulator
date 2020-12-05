package com.vb.market.engine.booking;

import java.time.Instant;

public class Trade {

    private Long tradeId;

    private Long buyEventId;

    private Long sellEventId;

    private Integer tradeQuantity;

    private Integer tradePrice;

    private Instant submittedTime;

    private String symbol;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
    }

    public Long getBuyEventId() {
        return buyEventId;
    }

    public void setBuyEventId(Long buyEventId) {
        this.buyEventId = buyEventId;
    }

    public Long getSellEventId() {
        return sellEventId;
    }

    public void setSellEventId(Long sellEventId) {
        this.sellEventId = sellEventId;
    }

    public Integer getTradeQuantity() {
        return tradeQuantity;
    }

    public void setTradeQuantity(Integer tradeQuantity) {
        this.tradeQuantity = tradeQuantity;
    }

    public Integer getTradePrice() {
        return tradePrice;
    }

    public void setTradePrice(Integer tradePrice) {
        this.tradePrice = tradePrice;
    }

    public Instant getSubmittedTime() {
        return submittedTime;
    }

    public void setSubmittedTime(Instant submittedTime) {
        this.submittedTime = submittedTime;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "tradeId=" + tradeId +
                ", buyEventId=" + buyEventId +
                ", sellEventId=" + sellEventId +
                ", tradeQuantity=" + tradeQuantity +
                ", tradePrice=" + tradePrice +
                ", submittedTime=" + submittedTime +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}
