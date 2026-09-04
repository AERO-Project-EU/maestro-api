package eu.orchestrator.common.exception;

import eu.orchestrator.common.enums.GenericMessage;

public class GenericBusinessException extends RuntimeException {

    private final GenericMessage genericMessage;

    public GenericBusinessException(String errorMessage, GenericMessage genericMessage) {
        super(errorMessage);
        this.genericMessage = genericMessage;
    }

    public GenericMessage getGenericMessage() {
        return genericMessage;
    }
}
