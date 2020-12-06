package com.vb.market.domain;

import com.vb.market.controller.Status;

public class CancelOrderResponse {

    private CancelOrderRequest cancelOrderRequest;
    private Status status;
    private String reason;

    public CancelOrderRequest getCancelOrderRequest() {
        return cancelOrderRequest;
    }

    public void setCancelOrderRequest(CancelOrderRequest cancelOrderRequest) {
        this.cancelOrderRequest = cancelOrderRequest;
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
        private CancelOrderRequest cancelOrderRequest;
        private Status status;
        private String reason;

        private Builder() {
        }

        public static Builder aCancelOrderResponse() {
            return new Builder();
        }

        public Builder withCancelOrderRequest(CancelOrderRequest cancelOrderRequest) {
            this.cancelOrderRequest = cancelOrderRequest;
            return this;
        }

        public Builder withMessage(Status status) {
            this.status = status;
            return this;
        }

        public Builder withReason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder but() {
            return aCancelOrderResponse().withCancelOrderRequest(cancelOrderRequest).withMessage(status).withReason(reason);
        }

        public CancelOrderResponse build() {
            CancelOrderResponse cancelOrderResponse = new CancelOrderResponse();
            cancelOrderResponse.setCancelOrderRequest(cancelOrderRequest);
            cancelOrderResponse.setStatus(status);
            cancelOrderResponse.setReason(reason);
            return cancelOrderResponse;
        }
    }
}
