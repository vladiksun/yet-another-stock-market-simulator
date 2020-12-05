package com.vb.market.domain;

public class CancelOrderResponse {

    private CancelOrderRequest cancelOrderRequest;
    private String message;
    private String reason;

    public CancelOrderRequest getCancelOrderRequest() {
        return cancelOrderRequest;
    }

    public void setCancelOrderRequest(CancelOrderRequest cancelOrderRequest) {
        this.cancelOrderRequest = cancelOrderRequest;
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
        private CancelOrderRequest cancelOrderRequest;
        private String message;
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

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withReason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder but() {
            return aCancelOrderResponse().withCancelOrderRequest(cancelOrderRequest).withMessage(message).withReason(reason);
        }

        public CancelOrderResponse build() {
            CancelOrderResponse cancelOrderResponse = new CancelOrderResponse();
            cancelOrderResponse.setCancelOrderRequest(cancelOrderRequest);
            cancelOrderResponse.setMessage(message);
            cancelOrderResponse.setReason(reason);
            return cancelOrderResponse;
        }
    }
}
