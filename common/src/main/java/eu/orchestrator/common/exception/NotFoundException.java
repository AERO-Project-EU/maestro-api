package eu.orchestrator.common.exception;

import eu.orchestrator.common.enums.GenericMessage;

public class NotFoundException extends RuntimeException {

    private final GenericMessage genericMessage;

    public NotFoundException(String errorMessage, GenericMessage genericMessage) {
        super(errorMessage);
        this.genericMessage = genericMessage;
    }

    public GenericMessage getGenericMessage() {
        return genericMessage;
    }

}
