package com.vb.market.exceptions;

import static java.lang.String.format;

public enum CommonCause implements ApplicationCause {

    SYMBOL_NOT_EXISTS(101, "Symbol does not exist"),
    ORDER_NOT_FOUND(102, "Order not found");

    private final int code;
    private String description;

    CommonCause(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String formatErrorMessage() {
        return format("%s: %s", getCode(), getDescription());
    }
}
