package eu.orchestrator.metric.exporter.exception;

public class MissingMetricsException extends RuntimeException{

    public MissingMetricsException() {
    }

    public MissingMetricsException(String message) {
        super(message);
    }

    public MissingMetricsException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingMetricsException(Throwable cause) {
        super(cause);
    }

    public MissingMetricsException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
