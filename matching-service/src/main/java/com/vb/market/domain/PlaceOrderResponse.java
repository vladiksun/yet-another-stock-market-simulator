package com.vb.market.domain;

import com.vb.market.controller.Status;

public class PlaceOrderResponse {

    private PlaceOrderRequest requestedPlaceOrderRequest;
    private Status status;
    private String reason;

    public PlaceOrderRequest getRequestedOrder() {
        return requestedPlaceOrderRequest;
    }

    public void setRequestedOrder(PlaceOrderRequest requestedPlaceOrderRequest) {
        this.requestedPlaceOrderRequest = requestedPlaceOrderRequest;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }


    public static final class Builder {
        private PlaceOrderRequest placeOrderRequest;
        private Status status;
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

        public Builder withStatus(Status status) {
            this.status = status;
            return this;
        }

        public Builder withReason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder but() {
            return anOrderOperationStatus().withRequestedOrder(placeOrderRequest).withStatus(status).withReason(reason);
        }

        public PlaceOrderResponse build() {
            PlaceOrderResponse placeOrderResponse = new PlaceOrderResponse();
            placeOrderResponse.setRequestedOrder(placeOrderRequest);
            placeOrderResponse.setStatus(status);
            placeOrderResponse.setReason(reason);
            return placeOrderResponse;
        }
    }
}
