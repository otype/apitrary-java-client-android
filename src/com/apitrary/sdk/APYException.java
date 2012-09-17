package com.apitrary.sdk;

/**
 * Exception used to indicate that something didn't went as expected when trying
 * to work with the backend.
 */
public class APYException extends Exception {

    /**
     * Enumeration of detail codes used to add more detail info to an {@link APYException}.
     */
    enum APYExceptionDetailCode {

        /**
         * Indicates that no detail code was specified.
         */
        NOT_SPECIFIED,

        /**
         * Indicates that an entity could not be found for a given ID.
         */
        ENTITY_NOT_FOUND,

        /**
         * Indicates that there was an error on the backend side.
         */
        BACKEND_ERROR
    }

    private static final long serialVersionUID = -6725646414103116447L;

    private APYExceptionDetailCode detailCode = APYExceptionDetailCode.NOT_SPECIFIED;

    public APYException() {}

    public APYException(APYExceptionDetailCode detailCode) {
        this.detailCode = detailCode;
    }

    public APYException(String message) {
        super(message);
    }

    public APYException(Throwable cause) {
        super(cause);
    }

    public APYException(APYExceptionDetailCode detailCode, String message) {
        super(message);
        this.detailCode = detailCode;
    }

    public APYException(APYExceptionDetailCode detailCode, Throwable cause) {
        super(cause);
        this.detailCode = detailCode;
    }

    public APYException(String message, Throwable cause) {
        super(message, cause);
    }

    public APYException(APYExceptionDetailCode detailCode, String message, Throwable cause) {
        super(message, cause);
        this.detailCode = detailCode;
    }

    /**
     * Returns the {@link APYException}'s detail code.
     *
     * @return the {@link APYException}'s detail code
     */
    public APYExceptionDetailCode getDetailCode() {
        return detailCode;
    }

}
