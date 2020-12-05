package com.vb.market.domain;

public class PlaceOrderResponse {

    private PlaceOrderRequest requestedPlaceOrderRequest;
    private String message;
    private String reason;

    public PlaceOrderRequest getRequestedOrder() {
        return requestedPlaceOrderRequest;
    }

    public void setRequestedOrder(PlaceOrderRequest requestedPlaceOrderRequest) {
        this.requestedPlaceOrderRequest = requestedPlaceOrderRequest;
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
        private PlaceOrderRequest placeOrderRequest;
        private String message;
        private String reason;

        private Builder() {
        }

        public static Builder anOrderOperationStatus() {
            return new Builder();
        }

        public Builder withRequestedOrder(PlaceOrderRequest requestedPlaceOrderRequest) {
            this.placeOrderRequest = requestedPlaceOrderRequest;
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
            return anOrderOperationStatus().withRequestedOrder(placeOrderRequest).withMessage(message).withReason(reason);
        }

        public PlaceOrderResponse build() {
            PlaceOrderResponse placeOrderResponse = new PlaceOrderResponse();
            placeOrderResponse.setRequestedOrder(placeOrderRequest);
            placeOrderResponse.setMessage(message);
            placeOrderResponse.setReason(reason);
            return placeOrderResponse;
        }
    }
}
