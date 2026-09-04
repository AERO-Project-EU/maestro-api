package eu.orchestrator.common.exception;

import eu.orchestrator.common.enums.GenericMessage;

public class BadRequestBusinessException extends RuntimeException {

    private final GenericMessage genericMessage;

    public BadRequestBusinessException(String errorMessage, GenericMessage genericMessage) {
        super(errorMessage);
        this.genericMessage = genericMessage;
    }

    public GenericMessage getGenericMessage() {
        return genericMessage;
    }
}
