package eu.orchestrator.metric.exporter.exception;

public class MissingMetricValuesException extends RuntimeException {

    public MissingMetricValuesException() {
    }

    public MissingMetricValuesException(String message) {
        super(message);
    }

    public MissingMetricValuesException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingMetricValuesException(Throwable cause) {
        super(cause);
    }

    public MissingMetricValuesException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
