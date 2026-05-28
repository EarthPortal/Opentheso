package fr.cnrs.opentheso.ws.openapi.exception;

public class ApiKeyMissingException extends RuntimeException {
    public ApiKeyMissingException() {
        super("Clé API absente");
    }
}
