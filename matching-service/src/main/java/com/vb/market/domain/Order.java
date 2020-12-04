package com.vb.market.domain;

public class Order {

    private String clientId;
    private Long eventId;
    private String symbol;
    private Integer price;
    private Integer quantity;
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

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }


    public static final class Builder {
        private String clientId;
        private Long eventId;
        private String symbol;
        private Integer price;
        private Integer quantity;
        private Side side;

        private Builder() {
        }

        public static Builder anOrderRequest() {
            return new Builder();
        }

        public Builder withClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder withOrderId(Long orderId) {
            this.eventId = orderId;
            return this;
        }

        public Builder withSymbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder withPrice(Integer price) {
            this.price = price;
            return this;
        }

        public Builder withQuantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder withSide(Side side) {
            this.side = side;
            return this;
        }

        public Builder but() {
            return anOrderRequest().withClientId(clientId).withOrderId(eventId).withSymbol(symbol).withPrice(price).withQuantity(quantity).withSide(side);
        }

        public Order build() {
            Order order = new Order();
            order.setClientId(clientId);
            order.setEventId(eventId);
            order.setSymbol(symbol);
            order.setPrice(price);
            order.setQuantity(quantity);
            order.setSide(side);
            return order;
        }
    }
}
