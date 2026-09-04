package eu.orchestrator.common.exception;

import eu.orchestrator.common.enums.GenericMessage;

public class NotAuthorizedException extends RuntimeException {

    private final GenericMessage genericMessage;

    public NotAuthorizedException(String errorMessage, GenericMessage genericMessage) {
        super(errorMessage);
        this.genericMessage = genericMessage;
    }

    public GenericMessage getGenericMessage() {
        return genericMessage;
    }

}
