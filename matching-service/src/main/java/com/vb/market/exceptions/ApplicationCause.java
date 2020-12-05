package com.vb.market.exceptions;

public interface ApplicationCause {

    int getCode();

    String getDescription();

    String formatErrorMessage();
}
