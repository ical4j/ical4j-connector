package org.ical4j.connector.dav.response;

/**
 * Thrown by a response handler when the server returns a status code the handler can't process,
 * such as a 404 in place of a multistatus response.
 */
public class UnexpectedStatusException extends RuntimeException {

    private final int statusCode;

    public UnexpectedStatusException(int statusCode) {
        super("Unexpected status code: " + statusCode);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
