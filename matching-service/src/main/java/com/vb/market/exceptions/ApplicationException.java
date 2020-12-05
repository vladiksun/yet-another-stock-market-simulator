package com.vb.market.exceptions;

public class ApplicationException extends RuntimeException {

    private ApplicationCause cause;

    public ApplicationException(ApplicationCause applicationCause) {
        this.cause = applicationCause;
    }

    public ApplicationException(ApplicationCause applicationCause, String message) {
        super(message);
        this.cause = applicationCause;
    }

    public ApplicationException(ApplicationCause applicationCause, Throwable cause, String message) {
        super(message, cause);
        this.cause = applicationCause;
    }

    public ApplicationException(ApplicationCause applicationCause, Throwable cause) {

        super(cause);
        this.cause = applicationCause;
    }
}

