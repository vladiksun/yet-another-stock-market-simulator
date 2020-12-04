package com.vb.market.domain;

public class OrderOperationStatus {

    private Order requestedOrder;
    private String message;
    private String reason;

    public Order getRequestedOrder() {
        return requestedOrder;
    }

    public void setRequestedOrder(Order requestedOrder) {
        this.requestedOrder = requestedOrder;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


    public static final class Builder {
        private Order requestedOrder;
        private String message;
        private String reason;

        private Builder() {
        }

        public static Builder anOrderOperationStatus() {
            return new Builder();
        }

        public Builder withRequestedOrder(Order requestedOrder) {
            this.requestedOrder = requestedOrder;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withReason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder but() {
            return anOrderOperationStatus().withRequestedOrder(requestedOrder).withMessage(message).withReason(reason);
        }

        public OrderOperationStatus build() {
            OrderOperationStatus orderOperationStatus = new OrderOperationStatus();
            orderOperationStatus.setRequestedOrder(requestedOrder);
            orderOperationStatus.setMessage(message);
            orderOperationStatus.setReason(reason);
            return orderOperationStatus;
        }
    }
}
