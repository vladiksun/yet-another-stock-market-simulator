package com.vb.market.domain;

import javax.validation.constraints.NotNull;

public class CancelOrderRequest {

    @NotNull
    private String clientId;

    @NotNull
    private Long eventId;

    @NotNull
    private String symbol;

    @NotNull
    private Side side;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    @Override
    public String toString() {
        return "CancelOrderRequest{" +
                "clientId='" + clientId + '\'' +
                ", eventId=" + eventId +
                ", symbol='" + symbol + '\'' +
                ", side=" + side +
                '}';
    }
}
