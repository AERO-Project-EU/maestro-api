package eu.orchestrator.metric.exporter.exception;

public class EmptyResponseException extends RuntimeException {

    public EmptyResponseException() {
    }

    public EmptyResponseException(String message) {
        super(message);
    }

    public EmptyResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyResponseException(Throwable cause) {
        super(cause);
    }

    public EmptyResponseException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
